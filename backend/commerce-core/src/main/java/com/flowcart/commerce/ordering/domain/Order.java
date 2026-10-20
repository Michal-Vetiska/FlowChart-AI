package com.flowcart.commerce.ordering.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.Money;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class Order {

	private final UUID id;
	private final UUID customerId;
	private OrderStatus status;
	private final String currency;
	private Money subtotalAmount;
	private Money discountAmount;
	private Money totalAmount;
	private String couponCode;
	private final String idempotencyKey;
	private final List<OrderItem> items;
	private final List<OrderStatusChange> statusHistory;
	private final Instant createdAt;
	private Instant updatedAt;

	private Order(
			UUID id,
			UUID customerId,
			OrderStatus status,
			String currency,
			Money subtotalAmount,
			Money discountAmount,
			Money totalAmount,
			String couponCode,
			String idempotencyKey,
			List<OrderItem> items,
			List<OrderStatusChange> statusHistory,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.customerId = Objects.requireNonNull(customerId);
		this.status = Objects.requireNonNull(status);
		this.currency = Objects.requireNonNull(currency);
		this.subtotalAmount = Objects.requireNonNull(subtotalAmount);
		this.discountAmount = Objects.requireNonNull(discountAmount);
		this.totalAmount = Objects.requireNonNull(totalAmount);
		this.couponCode = couponCode;
		this.idempotencyKey = idempotencyKey;
		this.items = new ArrayList<>(items);
		this.statusHistory = new ArrayList<>(statusHistory);
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static Order create(
			UUID customerId,
			String currency,
			List<OrderItem> items,
			Money discountAmount,
			String couponCode,
			String idempotencyKey
	) {
		if (items == null || items.isEmpty()) {
			throw new DomainException("Order must contain at least one item");
		}
		Money subtotal = Money.zero(currency);
		for (OrderItem item : items) {
			if (!item.unitPrice().currencyCode().equals(currency)) {
				throw new DomainException("Order item currency mismatch");
			}
			subtotal = subtotal.add(item.lineTotal());
		}
		Money discount = discountAmount == null ? Money.zero(currency) : discountAmount;
		if (discount.isNegative()) {
			throw new DomainException("Discount cannot be negative");
		}
		Money total = subtotal.subtract(discount);
		if (total.isNegative()) {
			total = Money.zero(currency);
		}
		Instant now = Instant.now();
		Order order = new Order(
				UUID.randomUUID(),
				customerId,
				OrderStatus.CREATED,
				currency,
				subtotal,
				discount,
				total,
				couponCode,
				idempotencyKey,
				items,
				List.of(),
				now,
				now
		);
		order.recordTransition(null, OrderStatus.CREATED, "Order created");
		return order;
	}

	public static Order rehydrate(
			UUID id,
			UUID customerId,
			OrderStatus status,
			String currency,
			Money subtotalAmount,
			Money discountAmount,
			Money totalAmount,
			String couponCode,
			String idempotencyKey,
			List<OrderItem> items,
			List<OrderStatusChange> statusHistory,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Order(
				id,
				customerId,
				status,
				currency,
				subtotalAmount,
				discountAmount,
				totalAmount,
				couponCode,
				idempotencyKey,
				items,
				statusHistory,
				createdAt,
				updatedAt
		);
	}

	public void transitionTo(OrderStatus target, String reason) {
		status.assertCanTransitionTo(target);
		OrderStatus previous = status;
		status = target;
		updatedAt = Instant.now();
		recordTransition(previous, target, reason);
	}

	private void recordTransition(OrderStatus from, OrderStatus to, String reason) {
		statusHistory.add(new OrderStatusChange(UUID.randomUUID(), from, to, reason, Instant.now()));
	}

	public UUID id() {
		return id;
	}

	public UUID customerId() {
		return customerId;
	}

	public OrderStatus status() {
		return status;
	}

	public String currency() {
		return currency;
	}

	public Money subtotalAmount() {
		return subtotalAmount;
	}

	public Money discountAmount() {
		return discountAmount;
	}

	public Money totalAmount() {
		return totalAmount;
	}

	public String couponCode() {
		return couponCode;
	}

	public String idempotencyKey() {
		return idempotencyKey;
	}

	public List<OrderItem> items() {
		return List.copyOf(items);
	}

	public List<OrderStatusChange> statusHistory() {
		return List.copyOf(statusHistory);
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
