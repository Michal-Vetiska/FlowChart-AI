package com.flowcart.commerce.catalog.adapters.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
		@NotBlank @Size(max = 120) String name,
		@Size(max = 4000) String description
) {
}
