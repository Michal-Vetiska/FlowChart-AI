package com.flowcart.commerce.ordering.domain;

import com.flowcart.commerce.shared.domain.InvalidStateTransitionException;
import java.util.EnumSet;
import java.util.Set;

public enum OrderStatus {
	CREATED,
	PENDING_RESERVATION,
	STOCK_RESERVED,
	STOCK_UNAVAILABLE,
	PAYMENT_PENDING,
	PAID,
	PAYMENT_FAILED,
	CONFIRMED,
	FULFILLMENT,
	COMPLETED,
	CANCELLED,
	EXPIRED;

	private static final Set<OrderStatus> TERMINAL = EnumSet.of(
			STOCK_UNAVAILABLE,
			COMPLETED,
			CANCELLED,
			EXPIRED
	);

	public boolean isTerminal() {
		return TERMINAL.contains(this);
	}

	public void assertCanTransitionTo(OrderStatus target) {
		boolean allowed = switch (this) {
			case CREATED -> target == PENDING_RESERVATION || target == CANCELLED;
			case PENDING_RESERVATION -> target == STOCK_RESERVED || target == STOCK_UNAVAILABLE || target == CANCELLED;
			case STOCK_RESERVED -> target == PAYMENT_PENDING || target == CANCELLED || target == EXPIRED;
			case PAYMENT_PENDING -> target == PAID || target == PAYMENT_FAILED || target == CANCELLED;
			case PAID -> target == CONFIRMED;
			case CONFIRMED -> target == FULFILLMENT || target == CANCELLED;
			case FULFILLMENT -> target == COMPLETED;
			case PAYMENT_FAILED -> target == CANCELLED;
			case STOCK_UNAVAILABLE, COMPLETED, CANCELLED, EXPIRED -> false;
		};
		if (!allowed) {
			throw new InvalidStateTransitionException("Cannot transition order from " + this + " to " + target);
		}
	}
}
