package com.flowcart.commerce.inventory.adapters.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataStockItemJpaRepository extends JpaRepository<StockItemJpaEntity, UUID> {

	Optional<StockItemJpaEntity> findByWarehouseIdAndProductId(UUID warehouseId, UUID productId);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select s from StockItemJpaEntity s where s.id = :id")
	Optional<StockItemJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
