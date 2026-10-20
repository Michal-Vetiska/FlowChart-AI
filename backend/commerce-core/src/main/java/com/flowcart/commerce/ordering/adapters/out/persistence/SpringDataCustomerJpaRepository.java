package com.flowcart.commerce.ordering.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCustomerJpaRepository extends JpaRepository<CustomerJpaEntity, UUID> {

	Optional<CustomerJpaEntity> findByEmail(String email);
}
