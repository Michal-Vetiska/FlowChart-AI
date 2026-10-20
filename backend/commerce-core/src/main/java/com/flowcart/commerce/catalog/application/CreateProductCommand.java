package com.flowcart.commerce.catalog.application;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductCommand(
		String sku,
		String name,
		String description,
		BigDecimal basePrice,
		String currency,
		UUID categoryId
) {
}
