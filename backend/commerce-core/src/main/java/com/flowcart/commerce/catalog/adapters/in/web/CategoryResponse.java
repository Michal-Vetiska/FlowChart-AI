package com.flowcart.commerce.catalog.adapters.in.web;

import com.flowcart.commerce.catalog.domain.Category;
import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
		UUID id,
		String name,
		String slug,
		String description,
		Instant createdAt,
		Instant updatedAt
) {
	public static CategoryResponse from(Category category) {
		return new CategoryResponse(
				category.id(),
				category.name(),
				category.slug(),
				category.description(),
				category.createdAt(),
				category.updatedAt()
		);
	}
}
