package com.flowcart.commerce.catalog.adapters.in.web;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(
		@NotBlank @Size(min = 3, max = 64) @Pattern(regexp = "[A-Za-z0-9-]+") String sku,
		@NotBlank @Size(max = 200) String name,
		@Size(max = 4000) String description,
		@NotNull @DecimalMin(value = "0.01") BigDecimal basePrice,
		@NotBlank @Size(min = 3, max = 3) String currency,
		UUID categoryId
) {
}
