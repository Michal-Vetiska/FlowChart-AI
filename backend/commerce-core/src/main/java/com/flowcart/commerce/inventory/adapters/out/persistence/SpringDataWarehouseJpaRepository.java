package com.flowcart.commerce.inventory.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataWarehouseJpaRepository extends JpaRepository<WarehouseJpaEntity, UUID> {

	Optional<WarehouseJpaEntity> findByCode(String code);

	boolean existsByCode(String code);
}
