package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.application.port.StockItemRepository;
import com.flowcart.commerce.inventory.domain.StockItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StockItemRepositoryAdapter implements StockItemRepository {

	private final SpringDataStockItemJpaRepository jpaRepository;

	public StockItemRepositoryAdapter(SpringDataStockItemJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public StockItem save(StockItem stockItem) {
		return InventoryPersistenceMapper.toDomain(jpaRepository.save(InventoryPersistenceMapper.toEntity(stockItem)));
	}

	@Override
	public Optional<StockItem> findById(UUID id) {
		return jpaRepository.findById(id).map(InventoryPersistenceMapper::toDomain);
	}

	@Override
	public Optional<StockItem> findByWarehouseAndProduct(UUID warehouseId, UUID productId) {
		return jpaRepository.findByWarehouseIdAndProductId(warehouseId, productId)
				.map(InventoryPersistenceMapper::toDomain);
	}

	@Override
	public Optional<StockItem> findByIdForUpdate(UUID id) {
		return jpaRepository.findByIdForUpdate(id).map(InventoryPersistenceMapper::toDomain);
	}

	@Override
	public List<StockItem> findAll() {
		return jpaRepository.findAll().stream().map(InventoryPersistenceMapper::toDomain).toList();
	}
}
