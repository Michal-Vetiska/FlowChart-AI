package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.application.port.StockMovementRepository;
import com.flowcart.commerce.inventory.domain.StockMovement;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StockMovementRepositoryAdapter implements StockMovementRepository {

	private final SpringDataStockMovementJpaRepository jpaRepository;

	public StockMovementRepositoryAdapter(SpringDataStockMovementJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public StockMovement save(StockMovement movement) {
		return InventoryPersistenceMapper.toDomain(jpaRepository.save(InventoryPersistenceMapper.toEntity(movement)));
	}

	@Override
	public List<StockMovement> findByStockItemId(UUID stockItemId) {
		return jpaRepository.findByStockItemIdOrderByCreatedAtDesc(stockItemId).stream()
				.map(InventoryPersistenceMapper::toDomain)
				.toList();
	}
}
