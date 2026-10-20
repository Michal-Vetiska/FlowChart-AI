package com.flowcart.commerce.messaging.adapters.out.persistence;

import com.flowcart.commerce.messaging.domain.SagaState;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_sagas")
public class OrderSagaJpaEntity {

	@Id
	@Column(name = "order_id")
	private UUID orderId;

	@Enumerated(EnumType.STRING)
	@Column(name = "saga_state", nullable = false, length = 64)
	private SagaState sagaState;

	@Column(name = "correlation_id", nullable = false)
	private UUID correlationId;

	@Column(name = "payment_outcome", length = 32)
	private String paymentOutcome;

	@Column(name = "last_error", columnDefinition = "TEXT")
	private String lastError;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	public OrderSagaJpaEntity() {
	}

	public UUID getOrderId() {
		return orderId;
	}

	public void setOrderId(UUID orderId) {
		this.orderId = orderId;
	}

	public SagaState getSagaState() {
		return sagaState;
	}

	public void setSagaState(SagaState sagaState) {
		this.sagaState = sagaState;
	}

	public UUID getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(UUID correlationId) {
		this.correlationId = correlationId;
	}

	public String getPaymentOutcome() {
		return paymentOutcome;
	}

	public void setPaymentOutcome(String paymentOutcome) {
		this.paymentOutcome = paymentOutcome;
	}

	public String getLastError() {
		return lastError;
	}

	public void setLastError(String lastError) {
		this.lastError = lastError;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Instant updatedAt) {
		this.updatedAt = updatedAt;
	}
}
