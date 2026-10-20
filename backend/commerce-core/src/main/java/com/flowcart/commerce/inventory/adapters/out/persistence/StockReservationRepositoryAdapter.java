package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.application.port.StockReservationRepository;
import com.flowcart.commerce.inventory.domain.ReservationStatus;
import com.flowcart.commerce.inventory.domain.StockReservation;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StockReservationRepositoryAdapter implements StockReservationRepository {

	private final SpringDataStockReservationJpaRepository jpaRepository;

	public StockReservationRepositoryAdapter(SpringDataStockReservationJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public StockReservation save(StockReservation reservation) {
		return InventoryPersistenceMapper.toDomain(jpaRepository.save(InventoryPersistenceMapper.toEntity(reservation)));
	}

	@Override
	public Optional<StockReservation> findById(UUID id) {
		return jpaRepository.findById(id).map(InventoryPersistenceMapper::toDomain);
	}

	@Override
	public List<StockReservation> findActiveByOrderId(UUID orderId) {
		return jpaRepository.findByOrderIdAndStatus(orderId, ReservationStatus.ACTIVE).stream()
				.map(InventoryPersistenceMapper::toDomain)
				.toList();
	}

	@Override
	public List<StockReservation> findExpiredActive(Instant now) {
		return jpaRepository.findExpiredActive(ReservationStatus.ACTIVE, now).stream()
				.map(InventoryPersistenceMapper::toDomain)
				.toList();
	}
}
