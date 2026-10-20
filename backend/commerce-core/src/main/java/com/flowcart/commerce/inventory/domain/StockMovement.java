package com.flowcart.commerce.inventory.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StockMovement {

	private final UUID id;
	private final UUID stockItemId;
	private final StockMovementType movementType;
	private final int quantity;
	private final String reason;
	private final UUID referenceId;
	private final Instant createdAt;

	private StockMovement(
			UUID id,
			UUID stockItemId,
			StockMovementType movementType,
			int quantity,
			String reason,
			UUID referenceId,
			Instant createdAt
	) {
		this.id = Objects.requireNonNull(id);
		this.stockItemId = Objects.requireNonNull(stockItemId);
		this.movementType = Objects.requireNonNull(movementType);
		this.quantity = quantity;
		this.reason = reason;
		this.referenceId = referenceId;
		this.createdAt = Objects.requireNonNull(createdAt);
	}

	public static StockMovement create(
			UUID stockItemId,
			StockMovementType movementType,
			int quantity,
			String reason,
			UUID referenceId
	) {
		return new StockMovement(UUID.randomUUID(), stockItemId, movementType, quantity, reason, referenceId, Instant.now());
	}

	public static StockMovement rehydrate(
			UUID id,
			UUID stockItemId,
			StockMovementType movementType,
			int quantity,
			String reason,
			UUID referenceId,
			Instant createdAt
	) {
		return new StockMovement(id, stockItemId, movementType, quantity, reason, referenceId, createdAt);
	}

	public UUID id() {
		return id;
	}

	public UUID stockItemId() {
		return stockItemId;
	}

	public StockMovementType movementType() {
		return movementType;
	}

	public int quantity() {
		return quantity;
	}

	public String reason() {
		return reason;
	}

	public UUID referenceId() {
		return referenceId;
	}

	public Instant createdAt() {
		return createdAt;
	}
}
