package com.flowcart.commerce.catalog.adapters.in.web;

import com.flowcart.commerce.catalog.domain.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ProductResponse(
		UUID id,
		String sku,
		String name,
		String description,
		BigDecimal basePrice,
		String currency,
		boolean active,
		UUID categoryId,
		Instant createdAt,
		Instant updatedAt
) {
	public static ProductResponse from(Product product) {
		return new ProductResponse(
				product.id(),
				product.sku(),
				product.name(),
				product.description(),
				product.basePrice().amount(),
				product.basePrice().currencyCode(),
				product.active(),
				product.categoryId(),
				product.createdAt(),
				product.updatedAt()
		);
	}
}
