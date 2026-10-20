package com.flowcart.commerce.inventory.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class StockItem {

	private final UUID id;
	private final UUID warehouseId;
	private final UUID productId;
	private int availableQty;
	private int reservedQty;
	private long version;
	private final Instant createdAt;
	private Instant updatedAt;

	private StockItem(
			UUID id,
			UUID warehouseId,
			UUID productId,
			int availableQty,
			int reservedQty,
			long version,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.warehouseId = Objects.requireNonNull(warehouseId);
		this.productId = Objects.requireNonNull(productId);
		if (availableQty < 0 || reservedQty < 0) {
			throw new DomainException("Stock quantities cannot be negative");
		}
		this.availableQty = availableQty;
		this.reservedQty = reservedQty;
		this.version = version;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static StockItem create(UUID warehouseId, UUID productId, int initialAvailable) {
		if (initialAvailable < 0) {
			throw new DomainException("Initial available quantity cannot be negative");
		}
		Instant now = Instant.now();
		return new StockItem(UUID.randomUUID(), warehouseId, productId, initialAvailable, 0, 0, now, now);
	}

	public static StockItem rehydrate(
			UUID id,
			UUID warehouseId,
			UUID productId,
			int availableQty,
			int reservedQty,
			long version,
			Instant createdAt,
			Instant updatedAt
	) {
		return new StockItem(id, warehouseId, productId, availableQty, reservedQty, version, createdAt, updatedAt);
	}

	public void adjust(int delta) {
		int next = availableQty + delta;
		if (next < 0) {
			throw new DomainException("Adjustment would make available stock negative");
		}
		availableQty = next;
		touch();
	}

	public void reserve(int quantity) {
		if (quantity <= 0) {
			throw new DomainException("Reservation quantity must be positive");
		}
		if (availableQty < quantity) {
			throw new DomainException("Insufficient available stock: requested=" + quantity + ", available=" + availableQty);
		}
		availableQty -= quantity;
		reservedQty += quantity;
		touch();
	}

	public void release(int quantity) {
		if (quantity <= 0) {
			throw new DomainException("Release quantity must be positive");
		}
		if (reservedQty < quantity) {
			throw new DomainException("Cannot release more than reserved quantity");
		}
		reservedQty -= quantity;
		availableQty += quantity;
		touch();
	}

	public void consumeReservation(int quantity) {
		if (quantity <= 0) {
			throw new DomainException("Consume quantity must be positive");
		}
		if (reservedQty < quantity) {
			throw new DomainException("Cannot consume more than reserved quantity");
		}
		reservedQty -= quantity;
		touch();
	}

	private void touch() {
		updatedAt = Instant.now();
	}

	public UUID id() {
		return id;
	}

	public UUID warehouseId() {
		return warehouseId;
	}

	public UUID productId() {
		return productId;
	}

	public int availableQty() {
		return availableQty;
	}

	public int reservedQty() {
		return reservedQty;
	}

	public long version() {
		return version;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
