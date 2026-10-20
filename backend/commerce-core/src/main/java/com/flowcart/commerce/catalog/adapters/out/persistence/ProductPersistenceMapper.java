package com.flowcart.commerce.catalog.adapters.out.persistence;

import com.flowcart.commerce.catalog.domain.Product;
import com.flowcart.commerce.shared.domain.Money;

final class ProductPersistenceMapper {

	private ProductPersistenceMapper() {
	}

	static ProductJpaEntity toEntity(Product product) {
		ProductJpaEntity entity = new ProductJpaEntity();
		entity.setId(product.id());
		entity.setSku(product.sku());
		entity.setName(product.name());
		entity.setDescription(product.description());
		entity.setBasePrice(product.basePrice().amount());
		entity.setCurrency(product.basePrice().currencyCode());
		entity.setActive(product.active());
		entity.setCategoryId(product.categoryId());
		entity.setCreatedAt(product.createdAt());
		entity.setUpdatedAt(product.updatedAt());
		return entity;
	}

	static Product toDomain(ProductJpaEntity entity) {
		return Product.rehydrate(
				entity.getId(),
				entity.getSku(),
				entity.getName(),
				entity.getDescription(),
				Money.of(entity.getBasePrice(), entity.getCurrency()),
				entity.isActive(),
				entity.getCategoryId(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
