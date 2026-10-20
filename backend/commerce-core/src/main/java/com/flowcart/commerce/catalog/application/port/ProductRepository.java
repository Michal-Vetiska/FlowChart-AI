package com.flowcart.commerce.catalog.application.port;

import com.flowcart.commerce.catalog.domain.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

	Product save(Product product);

	Optional<Product> findById(UUID id);

	Optional<Product> findBySku(String sku);

	List<Product> findAll();

	boolean existsBySku(String sku);
}
