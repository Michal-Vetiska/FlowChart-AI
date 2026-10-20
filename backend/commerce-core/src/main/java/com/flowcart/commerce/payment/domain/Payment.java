package com.flowcart.commerce.payment.domain;

import com.flowcart.commerce.shared.domain.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Payment {

	private final UUID id;
	private final UUID orderId;
	private final String idempotencyKey;
	private final Money amount;
	private PaymentStatus status;
	private String providerReference;
	private String failureReason;
	private final Instant createdAt;
	private Instant updatedAt;

	private Payment(
			UUID id,
			UUID orderId,
			String idempotencyKey,
			Money amount,
			PaymentStatus status,
			String providerReference,
			String failureReason,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.orderId = Objects.requireNonNull(orderId);
		this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
		this.amount = Objects.requireNonNull(amount);
		this.status = Objects.requireNonNull(status);
		this.providerReference = providerReference;
		this.failureReason = failureReason;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static Payment create(UUID orderId, String idempotencyKey, Money amount, PaymentStatus status,
			String providerReference, String failureReason) {
		Instant now = Instant.now();
		return new Payment(UUID.randomUUID(), orderId, idempotencyKey, amount, status, providerReference, failureReason, now, now);
	}

	public static Payment rehydrate(
			UUID id,
			UUID orderId,
			String idempotencyKey,
			Money amount,
			PaymentStatus status,
			String providerReference,
			String failureReason,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Payment(id, orderId, idempotencyKey, amount, status, providerReference, failureReason, createdAt, updatedAt);
	}

	public UUID id() {
		return id;
	}

	public UUID orderId() {
		return orderId;
	}

	public String idempotencyKey() {
		return idempotencyKey;
	}

	public Money amount() {
		return amount;
	}

	public PaymentStatus status() {
		return status;
	}

	public String providerReference() {
		return providerReference;
	}

	public String failureReason() {
		return failureReason;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
