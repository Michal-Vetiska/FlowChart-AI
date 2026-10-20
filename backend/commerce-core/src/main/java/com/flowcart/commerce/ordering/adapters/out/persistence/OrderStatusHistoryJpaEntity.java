package com.flowcart.commerce.ordering.adapters.out.persistence;

import com.flowcart.commerce.ordering.domain.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "order_status_history")
public class OrderStatusHistoryJpaEntity {

	@Id
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private OrderJpaEntity order;

	@Enumerated(EnumType.STRING)
	@Column(name = "from_status", length = 40)
	private OrderStatus fromStatus;

	@Enumerated(EnumType.STRING)
	@Column(name = "to_status", nullable = false, length = 40)
	private OrderStatus toStatus;

	@Column(length = 255)
	private String reason;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	protected OrderStatusHistoryJpaEntity() {
	}

	public UUID getId() { return id; }
	public void setId(UUID id) { this.id = id; }
	public OrderJpaEntity getOrder() { return order; }
	public void setOrder(OrderJpaEntity order) { this.order = order; }
	public OrderStatus getFromStatus() { return fromStatus; }
	public void setFromStatus(OrderStatus fromStatus) { this.fromStatus = fromStatus; }
	public OrderStatus getToStatus() { return toStatus; }
	public void setToStatus(OrderStatus toStatus) { this.toStatus = toStatus; }
	public String getReason() { return reason; }
	public void setReason(String reason) { this.reason = reason; }
	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
