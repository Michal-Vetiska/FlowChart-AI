package com.flowcart.commerce.ordering.adapters.out.persistence;

import com.flowcart.commerce.ordering.domain.OrderStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

	@Id
	private UUID id;

	@Column(name = "customer_id", nullable = false)
	private UUID customerId;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 40)
	private OrderStatus status;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal subtotalAmount;

	@Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal discountAmount;

	@Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal totalAmount;

	@Column(name = "coupon_code", length = 64)
	private String couponCode;

	@Column(name = "idempotency_key", unique = true, length = 128)
	private String idempotencyKey;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<OrderItemJpaEntity> items = new ArrayList<>();

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<OrderStatusHistoryJpaEntity> statusHistory = new ArrayList<>();

	protected OrderJpaEntity() {
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public UUID getCustomerId() { return customerId; }
	public void setCustomerId(UUID customerId) { this.customerId = customerId; }
	public OrderStatus getStatus() { return status; }
	public void setStatus(OrderStatus status) { this.status = status; }
	public String getCurrency() { return currency; }
	public void setCurrency(String currency) { this.currency = currency; }
	public BigDecimal getSubtotalAmount() { return subtotalAmount; }
	public void setSubtotalAmount(BigDecimal subtotalAmount) { this.subtotalAmount = subtotalAmount; }
	public BigDecimal getDiscountAmount() { return discountAmount; }
	public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
	public BigDecimal getTotalAmount() { return totalAmount; }
	public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
	public String getCouponCode() { return couponCode; }
	public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
	public String getIdempotencyKey() { return idempotencyKey; }
	public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
	public Instant getUpdatedAt() { return updatedAt; }
	public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
	public List<OrderItemJpaEntity> getItems() { return items; }
	public void setItems(List<OrderItemJpaEntity> items) { this.items = items; }
	public List<OrderStatusHistoryJpaEntity> getStatusHistory() { return statusHistory; }
	public void setStatusHistory(List<OrderStatusHistoryJpaEntity> statusHistory) { this.statusHistory = statusHistory; }
}
