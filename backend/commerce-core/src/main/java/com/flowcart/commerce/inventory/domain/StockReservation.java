package com.flowcart.commerce.inventory.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.InvalidStateTransitionException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StockReservation {

	private final UUID id;
	private final UUID stockItemId;
	private UUID orderId;
	private final int quantity;
	private ReservationStatus status;
	private final Instant expiresAt;
	private final Instant createdAt;
	private Instant updatedAt;

	private StockReservation(
			UUID id,
			UUID stockItemId,
			UUID orderId,
			int quantity,
			ReservationStatus status,
			Instant expiresAt,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.stockItemId = Objects.requireNonNull(stockItemId);
		this.orderId = orderId;
		if (quantity <= 0) {
			throw new DomainException("Reservation quantity must be positive");
		}
		this.quantity = quantity;
		this.status = Objects.requireNonNull(status);
		this.expiresAt = expiresAt;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static StockReservation create(UUID stockItemId, UUID orderId, int quantity, Instant expiresAt) {
		Instant now = Instant.now();
		return new StockReservation(
				UUID.randomUUID(),
				stockItemId,
				orderId,
				quantity,
				ReservationStatus.ACTIVE,
				expiresAt,
				now,
				now
		);
	}

	public static StockReservation rehydrate(
			UUID id,
			UUID stockItemId,
			UUID orderId,
			int quantity,
			ReservationStatus status,
			Instant expiresAt,
			Instant createdAt,
			Instant updatedAt
	) {
		return new StockReservation(id, stockItemId, orderId, quantity, status, expiresAt, createdAt, updatedAt);
	}

	public void release() {
		if (status != ReservationStatus.ACTIVE) {
			throw new InvalidStateTransitionException("Only ACTIVE reservations can be released");
		}
		status = ReservationStatus.RELEASED;
		updatedAt = Instant.now();
	}

	public void consume() {
		if (status != ReservationStatus.ACTIVE) {
			throw new InvalidStateTransitionException("Only ACTIVE reservations can be consumed");
		}
		status = ReservationStatus.CONSUMED;
		updatedAt = Instant.now();
	}

	public void expire() {
		if (status != ReservationStatus.ACTIVE) {
			throw new InvalidStateTransitionException("Only ACTIVE reservations can expire");
		}
		status = ReservationStatus.EXPIRED;
		updatedAt = Instant.now();
	}

	public boolean isExpiredAt(Instant moment) {
		return expiresAt != null && !moment.isBefore(expiresAt);
	}

	public void assignOrder(UUID orderId) {
		this.orderId = orderId;
		updatedAt = Instant.now();
	}

	public UUID id() {
		return id;
	}

	public UUID stockItemId() {
		return stockItemId;
	}

	public UUID orderId() {
		return orderId;
	}

	public int quantity() {
		return quantity;
	}

	public ReservationStatus status() {
		return status;
	}

	public Instant expiresAt() {
		return expiresAt;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
