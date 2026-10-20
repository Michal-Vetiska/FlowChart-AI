package com.flowcart.commerce.ordering.adapters.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
public class OrderItemJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderJpaEntity order;

	@Column(name = "product_id", nullable = false)
	private UUID productId;

	@Column(name = "warehouse_id", nullable = false)
	private UUID warehouseId;

	@Column(nullable = false, length = 64)
	private String sku;

	@Column(name = "product_name", nullable = false, length = 200)
	private String productName;

	@Column(nullable = false)
	private int quantity;

	@Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
	private BigDecimal unitPrice;

	@Column(name = "line_total", nullable = false, precision = 19, scale = 4)
	private BigDecimal lineTotal;

	protected OrderItemJpaEntity() {
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public OrderJpaEntity getOrder() { return order; }
	public void setOrder(OrderJpaEntity order) { this.order = order; }
	public UUID getProductId() { return productId; }
	public void setProductId(UUID productId) { this.productId = productId; }
	public UUID getWarehouseId() { return warehouseId; }
	public void setWarehouseId(UUID warehouseId) { this.warehouseId = warehouseId; }
	public String getSku() { return sku; }
	public void setSku(String sku) { this.sku = sku; }
	public String getProductName() { return productName; }
	public void setProductName(String productName) { this.productName = productName; }
	public int getQuantity() { return quantity; }
	public void setQuantity(int quantity) { this.quantity = quantity; }
	public BigDecimal getUnitPrice() { return unitPrice; }
	public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
	public BigDecimal getLineTotal() { return lineTotal; }
	public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }
}
