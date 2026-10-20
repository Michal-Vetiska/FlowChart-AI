package com.flowcart.commerce.catalog.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {

	Optional<CategoryJpaEntity> findBySlug(String slug);

	boolean existsBySlug(String slug);
}
