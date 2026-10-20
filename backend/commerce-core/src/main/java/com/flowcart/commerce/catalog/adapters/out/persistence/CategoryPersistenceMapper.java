package com.flowcart.commerce.catalog.adapters.out.persistence;

import com.flowcart.commerce.catalog.domain.Category;

final class CategoryPersistenceMapper {

	private CategoryPersistenceMapper() {
	}

	static CategoryJpaEntity toEntity(Category category) {
		CategoryJpaEntity entity = new CategoryJpaEntity();
		entity.setId(category.id());
		entity.setName(category.name());
		entity.setSlug(category.slug());
		entity.setDescription(category.description());
		entity.setCreatedAt(category.createdAt());
		entity.setUpdatedAt(category.updatedAt());
		return entity;
	}

	static Category toDomain(CategoryJpaEntity entity) {
		return Category.rehydrate(
				entity.getId(),
				entity.getName(),
				entity.getSlug(),
				entity.getDescription(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
