package com.flowcart.commerce.payment.adapters.out;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataPaymentJpaRepository extends JpaRepository<PaymentJpaEntity, UUID> {

	Optional<PaymentJpaEntity> findByIdempotencyKey(String idempotencyKey);

	Optional<PaymentJpaEntity> findByOrderId(UUID orderId);
}
