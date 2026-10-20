package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.domain.StockMovementType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_movements")
public class StockMovementJpaEntity {

	@Id
	private UUID id;

	@Column(name = "stock_item_id", nullable = false)
	private UUID stockItemId;

	@Column(name = "movement_type", nullable = false, length = 32)
	@Enumerated(EnumType.STRING)
	private StockMovementType movementType;

	@Column(nullable = false)
	private int quantity;

	@Column(length = 255)
	private String reason;

	@Column(name = "reference_id")
	private UUID referenceId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected StockMovementJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getStockItemId() {
		return stockItemId;
	}

	public void setStockItemId(UUID stockItemId) {
		this.stockItemId = stockItemId;
	}

	public StockMovementType getMovementType() {
		return movementType;
	}

	public void setMovementType(StockMovementType movementType) {
		this.movementType = movementType;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public UUID getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(UUID referenceId) {
		this.referenceId = referenceId;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}
}
