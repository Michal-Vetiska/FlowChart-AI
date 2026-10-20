package com.flowcart.commerce.payment.application.port;

import com.flowcart.commerce.payment.domain.Payment;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository {

	Payment save(Payment payment);

	Optional<Payment> findByIdempotencyKey(String idempotencyKey);

	Optional<Payment> findByOrderId(UUID orderId);
}
