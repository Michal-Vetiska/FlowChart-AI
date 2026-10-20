package com.flowcart.commerce.payment.adapters.out;

import com.flowcart.commerce.payment.application.port.PaymentRepository;
import com.flowcart.commerce.payment.domain.Payment;
import com.flowcart.commerce.shared.domain.Money;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class PaymentRepositoryAdapter implements PaymentRepository {

	private final SpringDataPaymentJpaRepository jpaRepository;

	public PaymentRepositoryAdapter(SpringDataPaymentJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public Payment save(Payment payment) {
		PaymentJpaEntity entity = new PaymentJpaEntity();
		entity.setId(payment.id());
		entity.setOrderId(payment.orderId());
		entity.setIdempotencyKey(payment.idempotencyKey());
		entity.setAmount(payment.amount().amount());
		entity.setCurrency(payment.amount().currencyCode());
		entity.setStatus(payment.status());
		entity.setProviderReference(payment.providerReference());
		entity.setFailureReason(payment.failureReason());
		entity.setCreatedAt(payment.createdAt());
		entity.setUpdatedAt(payment.updatedAt());
		PaymentJpaEntity saved = jpaRepository.save(entity);
		return toDomain(saved);
	}

	@Override
	public Optional<Payment> findByIdempotencyKey(String idempotencyKey) {
		return jpaRepository.findByIdempotencyKey(idempotencyKey).map(this::toDomain);
	}

	@Override
	public Optional<Payment> findByOrderId(UUID orderId) {
		return jpaRepository.findByOrderId(orderId).map(this::toDomain);
	}

	private Payment toDomain(PaymentJpaEntity entity) {
		return Payment.rehydrate(
				entity.getId(),
				entity.getOrderId(),
				entity.getIdempotencyKey(),
				Money.of(entity.getAmount(), entity.getCurrency()),
				entity.getStatus(),
				entity.getProviderReference(),
				entity.getFailureReason(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
