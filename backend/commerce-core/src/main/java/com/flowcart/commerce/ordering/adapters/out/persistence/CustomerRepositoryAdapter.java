package com.flowcart.commerce.ordering.adapters.out.persistence;

import com.flowcart.commerce.ordering.application.port.CustomerRepository;
import com.flowcart.commerce.ordering.domain.Customer;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerRepositoryAdapter implements CustomerRepository {

	private final SpringDataCustomerJpaRepository jpaRepository;

	public CustomerRepositoryAdapter(SpringDataCustomerJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Customer save(Customer customer) {
		return OrderingPersistenceMapper.toDomain(jpaRepository.save(OrderingPersistenceMapper.toEntity(customer)));
	}

	@Override
	public Optional<Customer> findById(UUID id) {
		return jpaRepository.findById(id).map(OrderingPersistenceMapper::toDomain);
	}

	@Override
	public Optional<Customer> findByEmail(String email) {
		return jpaRepository.findByEmail(email.trim().toLowerCase()).map(OrderingPersistenceMapper::toDomain);
	}
}
