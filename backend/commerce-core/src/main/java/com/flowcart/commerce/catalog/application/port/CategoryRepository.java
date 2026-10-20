package com.flowcart.commerce.catalog.application.port;

import com.flowcart.commerce.catalog.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

	Category save(Category category);

	Optional<Category> findById(UUID id);

	Optional<Category> findBySlug(String slug);

	List<Category> findAll();

	boolean existsBySlug(String slug);
}
