package com.flowcart.commerce.catalog.adapters.out.persistence;

import com.flowcart.commerce.catalog.application.port.ProductRepository;
import com.flowcart.commerce.catalog.domain.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ProductRepositoryAdapter implements ProductRepository {

	private final SpringDataProductJpaRepository jpaRepository;

	public ProductRepositoryAdapter(SpringDataProductJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Product save(Product product) {
		return ProductPersistenceMapper.toDomain(jpaRepository.save(ProductPersistenceMapper.toEntity(product)));
	}

	@Override
	public Optional<Product> findById(UUID id) {
		return jpaRepository.findById(id).map(ProductPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Product> findBySku(String sku) {
		return jpaRepository.findBySku(sku).map(ProductPersistenceMapper::toDomain);
	}

	@Override
	public List<Product> findAll() {
		return jpaRepository.findAll().stream().map(ProductPersistenceMapper::toDomain).toList();
	}

	@Override
	public boolean existsBySku(String sku) {
		return jpaRepository.existsBySku(sku.trim().toUpperCase());
	}
}
