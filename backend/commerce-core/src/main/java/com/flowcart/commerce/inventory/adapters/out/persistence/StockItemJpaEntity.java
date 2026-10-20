package com.flowcart.commerce.inventory.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "stock_items")
public class StockItemJpaEntity {

	@Id
	private UUID id;

	@Column(name = "warehouse_id", nullable = false)
	private UUID warehouseId;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "available_qty", nullable = false)
	private int availableQty;

	@Column(name = "reserved_qty", nullable = false)
	private int reservedQty;

	@Version
	@Column(nullable = false)
	private long version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected StockItemJpaEntity() {
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public UUID getWarehouseId() {
		return warehouseId;
	}

	public void setWarehouseId(UUID warehouseId) {
		this.warehouseId = warehouseId;
	}

	public UUID getProductId() {
		return productId;
	}

	public void setProductId(UUID productId) {
		this.productId = productId;
	}

	public int getAvailableQty() {
		return availableQty;
	}

	public void setAvailableQty(int availableQty) {
		this.availableQty = availableQty;
	}

	public int getReservedQty() {
		return reservedQty;
	}

	public void setReservedQty(int reservedQty) {
		this.reservedQty = reservedQty;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
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
