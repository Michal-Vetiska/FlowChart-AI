package com.flowcart.commerce.catalog.adapters.out.persistence;

import com.flowcart.commerce.catalog.application.port.CategoryRepository;
import com.flowcart.commerce.catalog.domain.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CategoryRepositoryAdapter implements CategoryRepository {

	private final SpringDataCategoryJpaRepository jpaRepository;

	public CategoryRepositoryAdapter(SpringDataCategoryJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Category save(Category category) {
		return CategoryPersistenceMapper.toDomain(jpaRepository.save(CategoryPersistenceMapper.toEntity(category)));
	}

	@Override
	public Optional<Category> findById(UUID id) {
		return jpaRepository.findById(id).map(CategoryPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Category> findBySlug(String slug) {
		return jpaRepository.findBySlug(slug).map(CategoryPersistenceMapper::toDomain);
	}

	@Override
	public List<Category> findAll() {
		return jpaRepository.findAll().stream().map(CategoryPersistenceMapper::toDomain).toList();
	}

	@Override
	public boolean existsBySlug(String slug) {
		return jpaRepository.existsBySlug(slug);
	}
}
