package com.flowcart.commerce.inventory.adapters.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataStockMovementJpaRepository extends JpaRepository<StockMovementJpaEntity, UUID> {

	List<StockMovementJpaEntity> findByStockItemIdOrderByCreatedAtDesc(UUID stockItemId);
}
