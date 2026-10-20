package com.flowcart.commerce.catalog.adapters.in.web;

import com.flowcart.commerce.catalog.application.CreateProductCommand;
import com.flowcart.commerce.catalog.application.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
		return ProductResponse.from(productService.create(new CreateProductCommand(
				request.sku(),
				request.name(),
				request.description(),
				request.basePrice(),
				request.currency().toUpperCase(),
				request.categoryId()
		)));
	}

	@GetMapping
	public List<ProductResponse> list() {
		return productService.list().stream().map(ProductResponse::from).toList();
	}

	@GetMapping("/{id}")
	public ProductResponse get(@PathVariable UUID id) {
		return ProductResponse.from(productService.getById(id));
	}

	@PostMapping("/{id}/activate")
	public ProductResponse activate(@PathVariable UUID id) {
		return ProductResponse.from(productService.activate(id));
	}

	@PostMapping("/{id}/deactivate")
	public ProductResponse deactivate(@PathVariable UUID id) {
		return ProductResponse.from(productService.deactivate(id));
	}
}
