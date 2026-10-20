package com.flowcart.commerce.catalog.application;

import com.flowcart.commerce.catalog.application.port.CategoryRepository;
import com.flowcart.commerce.catalog.application.port.ProductRepository;
import com.flowcart.commerce.catalog.domain.Product;
import com.flowcart.commerce.shared.domain.ConflictException;
import com.flowcart.commerce.shared.domain.Money;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

	private final ProductRepository productRepository;
	private final CategoryRepository categoryRepository;

	public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository) {
		this.productRepository = productRepository;
		this.categoryRepository = categoryRepository;
	}

	@Transactional
	public Product create(CreateProductCommand command) {
		if (productRepository.existsBySku(command.sku())) {
			throw new ConflictException("Product with SKU already exists: " + command.sku());
		}
		if (command.categoryId() != null && categoryRepository.findById(command.categoryId()).isEmpty()) {
			throw new ResourceNotFoundException("Category not found: " + command.categoryId());
		}
		Money price = Money.of(command.basePrice(), command.currency());
		Product product = Product.create(
				command.sku(),
				command.name(),
				command.description(),
				price,
				command.categoryId()
		);
		return productRepository.save(product);
	}

	@Transactional(readOnly = true)
	public Product getById(UUID id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
	}

	@Transactional(readOnly = true)
	public List<Product> list() {
		return productRepository.findAll();
	}

	@Transactional
	public Product activate(UUID id) {
		Product product = getById(id);
		product.activate();
		return productRepository.save(product);
	}

	@Transactional
	public Product deactivate(UUID id) {
		Product product = getById(id);
		product.deactivate();
		return productRepository.save(product);
	}
}
