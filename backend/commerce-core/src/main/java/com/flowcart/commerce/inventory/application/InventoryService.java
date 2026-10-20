package com.flowcart.commerce.inventory.application;

import com.flowcart.commerce.catalog.application.port.ProductRepository;
import com.flowcart.commerce.inventory.application.port.StockItemRepository;
import com.flowcart.commerce.inventory.application.port.StockMovementRepository;
import com.flowcart.commerce.inventory.application.port.StockReservationRepository;
import com.flowcart.commerce.inventory.application.port.WarehouseRepository;
import com.flowcart.commerce.inventory.domain.ReservationStatus;
import com.flowcart.commerce.inventory.domain.StockItem;
import com.flowcart.commerce.inventory.domain.StockMovement;
import com.flowcart.commerce.inventory.domain.StockMovementType;
import com.flowcart.commerce.inventory.domain.StockReservation;
import com.flowcart.commerce.inventory.domain.Warehouse;
import com.flowcart.commerce.chaos.ChaosService;
import com.flowcart.commerce.shared.domain.ConflictException;
import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import com.flowcart.commerce.shared.observability.CommerceMetrics;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {

	public static final Duration DEFAULT_RESERVATION_TTL = Duration.ofMinutes(15);

	private final WarehouseRepository warehouseRepository;
	private final StockItemRepository stockItemRepository;
	private final StockReservationRepository stockReservationRepository;
	private final StockMovementRepository stockMovementRepository;
	private final ProductRepository productRepository;
	private final ObjectProvider<ChaosService> chaosService;
	private final ObjectProvider<CommerceMetrics> commerceMetrics;

	public InventoryService(
			WarehouseRepository warehouseRepository,
			StockItemRepository stockItemRepository,
			StockReservationRepository stockReservationRepository,
			StockMovementRepository stockMovementRepository,
			ProductRepository productRepository,
			ObjectProvider<ChaosService> chaosService,
			ObjectProvider<CommerceMetrics> commerceMetrics
	) {
		this.warehouseRepository = warehouseRepository;
		this.stockItemRepository = stockItemRepository;
		this.stockReservationRepository = stockReservationRepository;
		this.stockMovementRepository = stockMovementRepository;
		this.productRepository = productRepository;
		this.chaosService = chaosService;
		this.commerceMetrics = commerceMetrics;
	}

	private void maybeInjectLatency() {
		ChaosService chaos = chaosService.getIfAvailable();
		if (chaos == null) {
			return;
		}
		long delay = chaos.inventoryDelayMs();
		if (delay <= 0) {
			return;
		}
		try {
			Thread.sleep(delay);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DomainException("Interrupted during chaos latency", e);
		}
	}

	@Transactional
	public Warehouse createWarehouse(String code, String name, String location) {
		if (warehouseRepository.existsByCode(code)) {
			throw new ConflictException("Warehouse code already exists: " + code);
		}
		return warehouseRepository.save(Warehouse.create(code, name, location));
	}

	@Transactional(readOnly = true)
	public List<Warehouse> listWarehouses() {
		return warehouseRepository.findAll();
	}

	@Transactional(readOnly = true)
	public Warehouse getWarehouse(UUID id) {
		return warehouseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Warehouse not found: " + id));
	}

	@Transactional
	public StockItem createOrAdjustStock(UUID warehouseId, UUID productId, int availableQty, String reason) {
		if (availableQty < 0) {
			throw new DomainException("availableQty must be >= 0");
		}
		getWarehouse(warehouseId);
		productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));

		return stockItemRepository.findByWarehouseAndProduct(warehouseId, productId)
				.map(existing -> {
					StockItem locked = stockItemRepository.findByIdForUpdate(existing.id())
							.orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + existing.id()));
					int delta = availableQty - locked.availableQty();
					if (delta != 0) {
						locked.adjust(delta);
						stockMovementRepository.save(StockMovement.create(
								locked.id(),
								delta > 0 ? StockMovementType.ADJUSTMENT_IN : StockMovementType.ADJUSTMENT_OUT,
								Math.abs(delta),
								reason == null ? "Stock adjustment" : reason,
								null
						));
					}
					return stockItemRepository.save(locked);
				})
				.orElseGet(() -> {
					StockItem created = StockItem.create(warehouseId, productId, availableQty);
					StockItem saved = stockItemRepository.save(created);
					if (availableQty > 0) {
						stockMovementRepository.save(StockMovement.create(
								saved.id(),
								StockMovementType.ADJUSTMENT_IN,
								availableQty,
								reason == null ? "Initial stock" : reason,
								null
						));
					}
					return saved;
				});
	}

	@Transactional(readOnly = true)
	public List<StockItem> listStock() {
		return stockItemRepository.findAll();
	}

	@Transactional
	public StockReservation reserve(UUID warehouseId, UUID productId, UUID orderId, int quantity) {
		maybeInjectLatency();
		StockItem stockItem = stockItemRepository.findByWarehouseAndProduct(warehouseId, productId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Stock not found for warehouse=" + warehouseId + ", product=" + productId));

		StockItem locked = stockItemRepository.findByIdForUpdate(stockItem.id())
				.orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + stockItem.id()));

		try {
			locked.reserve(quantity);
		}
		catch (DomainException ex) {
			CommerceMetrics metrics = commerceMetrics.getIfAvailable();
			if (metrics != null) {
				metrics.reservationFailed();
			}
			throw ex;
		}
		stockItemRepository.save(locked);

		StockReservation reservation = StockReservation.create(
				locked.id(),
				orderId,
				quantity,
				Instant.now().plus(DEFAULT_RESERVATION_TTL)
		);
		StockReservation saved = stockReservationRepository.save(reservation);

		stockMovementRepository.save(StockMovement.create(
				locked.id(),
				StockMovementType.RESERVATION,
				quantity,
				"Stock reserved for order",
				orderId
		));
		return saved;
	}

	@Transactional
	public void releaseReservationsForOrder(UUID orderId) {
		List<StockReservation> active = stockReservationRepository.findActiveByOrderId(orderId);
		for (StockReservation reservation : active) {
			if (reservation.status() != ReservationStatus.ACTIVE) {
				continue;
			}
			StockItem locked = stockItemRepository.findByIdForUpdate(reservation.stockItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + reservation.stockItemId()));
			locked.release(reservation.quantity());
			stockItemRepository.save(locked);
			reservation.release();
			stockReservationRepository.save(reservation);
			stockMovementRepository.save(StockMovement.create(
					locked.id(),
					StockMovementType.RESERVATION_RELEASE,
					reservation.quantity(),
					"Reservation released",
					orderId
			));
		}
	}

	@Transactional
	public void consumeReservationsForOrder(UUID orderId) {
		List<StockReservation> active = stockReservationRepository.findActiveByOrderId(orderId);
		for (StockReservation reservation : active) {
			StockItem locked = stockItemRepository.findByIdForUpdate(reservation.stockItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + reservation.stockItemId()));
			locked.consumeReservation(reservation.quantity());
			stockItemRepository.save(locked);
			reservation.consume();
			stockReservationRepository.save(reservation);
			stockMovementRepository.save(StockMovement.create(
					locked.id(),
					StockMovementType.SALE,
					reservation.quantity(),
					"Reservation consumed after payment",
					orderId
			));
		}
	}

	@Transactional(readOnly = true)
	public List<StockMovement> movementsForStockItem(UUID stockItemId) {
		return stockMovementRepository.findByStockItemId(stockItemId);
	}

	@Transactional(readOnly = true)
	public Optional<StockItem> findStock(UUID warehouseId, UUID productId) {
		return stockItemRepository.findByWarehouseAndProduct(warehouseId, productId);
	}

	@Transactional
	public int expireDueReservations() {
		Instant now = Instant.now();
		List<StockReservation> expired = stockReservationRepository.findExpiredActive(now);
		int count = 0;
		for (StockReservation reservation : expired) {
			StockItem locked = stockItemRepository.findByIdForUpdate(reservation.stockItemId())
					.orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + reservation.stockItemId()));
			locked.release(reservation.quantity());
			stockItemRepository.save(locked);
			reservation.expire();
			stockReservationRepository.save(reservation);
			stockMovementRepository.save(StockMovement.create(
					locked.id(),
					StockMovementType.RESERVATION_RELEASE,
					reservation.quantity(),
					"Reservation expired",
					reservation.orderId()
			));
			count++;
		}
		return count;
	}

	@Transactional
	public StockReservation reserveWithTtl(
			UUID warehouseId,
			UUID productId,
			UUID orderId,
			int quantity,
			Duration ttl
	) {
		StockItem stockItem = stockItemRepository.findByWarehouseAndProduct(warehouseId, productId)
				.orElseThrow(() -> new ResourceNotFoundException(
						"Stock not found for warehouse=" + warehouseId + ", product=" + productId));

		StockItem locked = stockItemRepository.findByIdForUpdate(stockItem.id())
				.orElseThrow(() -> new ResourceNotFoundException("Stock item not found: " + stockItem.id()));

		locked.reserve(quantity);
		stockItemRepository.save(locked);

		StockReservation reservation = StockReservation.create(
				locked.id(),
				orderId,
				quantity,
				Instant.now().plus(ttl)
		);
		StockReservation saved = stockReservationRepository.save(reservation);
		stockMovementRepository.save(StockMovement.create(
				locked.id(),
				StockMovementType.RESERVATION,
				quantity,
				"Stock reserved for order",
				orderId
		));
		return saved;
	}
}
