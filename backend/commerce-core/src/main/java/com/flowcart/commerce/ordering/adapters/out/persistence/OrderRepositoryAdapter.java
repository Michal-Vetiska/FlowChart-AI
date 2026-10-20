package com.flowcart.commerce.ordering.adapters.out.persistence;

import com.flowcart.commerce.ordering.application.port.OrderRepository;
import com.flowcart.commerce.ordering.domain.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class OrderRepositoryAdapter implements OrderRepository {

	private final SpringDataOrderJpaRepository jpaRepository;

	public OrderRepositoryAdapter(SpringDataOrderJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Order save(Order order) {
		return OrderingPersistenceMapper.toDomain(jpaRepository.save(OrderingPersistenceMapper.toEntity(order)));
	}

	@Override
	public Optional<Order> findById(UUID id) {
		return jpaRepository.findById(id).map(OrderingPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Order> findByIdempotencyKey(String idempotencyKey) {
		return jpaRepository.findByIdempotencyKey(idempotencyKey).map(OrderingPersistenceMapper::toDomain);
	}

	@Override
	public List<Order> findAll() {
		return jpaRepository.findAll().stream().map(OrderingPersistenceMapper::toDomain).toList();
	}
}
