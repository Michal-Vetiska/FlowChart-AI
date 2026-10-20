package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.application.port.WarehouseRepository;
import com.flowcart.commerce.inventory.domain.Warehouse;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class WarehouseRepositoryAdapter implements WarehouseRepository {

	private final SpringDataWarehouseJpaRepository jpaRepository;

	public WarehouseRepositoryAdapter(SpringDataWarehouseJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Warehouse save(Warehouse warehouse) {
		return InventoryPersistenceMapper.toDomain(jpaRepository.save(InventoryPersistenceMapper.toEntity(warehouse)));
	}

	@Override
	public Optional<Warehouse> findById(UUID id) {
		return jpaRepository.findById(id).map(InventoryPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Warehouse> findByCode(String code) {
		return jpaRepository.findByCode(code.trim().toUpperCase()).map(InventoryPersistenceMapper::toDomain);
	}

	@Override
	public List<Warehouse> findAll() {
		return jpaRepository.findAll().stream().map(InventoryPersistenceMapper::toDomain).toList();
	}

	@Override
	public boolean existsByCode(String code) {
		return jpaRepository.existsByCode(code.trim().toUpperCase());
	}
}
