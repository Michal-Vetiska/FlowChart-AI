package com.flowcart.commerce.ordering.application.port;

import com.flowcart.commerce.ordering.domain.Customer;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {

	Customer save(Customer customer);

	Optional<Customer> findById(UUID id);

	Optional<Customer> findByEmail(String email);
}
