package com.flowcart.commerce.catalog.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProductJpaRepository extends JpaRepository<ProductJpaEntity, UUID> {

	Optional<ProductJpaEntity> findBySku(String sku);

	boolean existsBySku(String sku);
}
