package com.flowcart.commerce.inventory.application.port;

import com.flowcart.commerce.inventory.domain.Warehouse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {

	Warehouse save(Warehouse warehouse);

	Optional<Warehouse> findById(UUID id);

	Optional<Warehouse> findByCode(String code);

	List<Warehouse> findAll();

	boolean existsByCode(String code);
}
