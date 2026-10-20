package com.flowcart.commerce.ordering.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flowcart.commerce.shared.domain.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

class OrderStatusTest {

	@Test
	void allowsHappyPath() {
		assertThatCode(() -> {
			OrderStatus.CREATED.assertCanTransitionTo(OrderStatus.PENDING_RESERVATION);
			OrderStatus.PENDING_RESERVATION.assertCanTransitionTo(OrderStatus.STOCK_RESERVED);
			OrderStatus.STOCK_RESERVED.assertCanTransitionTo(OrderStatus.PAYMENT_PENDING);
			OrderStatus.PAYMENT_PENDING.assertCanTransitionTo(OrderStatus.PAID);
			OrderStatus.PAID.assertCanTransitionTo(OrderStatus.CONFIRMED);
		}).doesNotThrowAnyException();
	}

	@Test
	void rejectsInvalidJump() {
		assertThatThrownBy(() -> OrderStatus.CREATED.assertCanTransitionTo(OrderStatus.PAID))
				.isInstanceOf(InvalidStateTransitionException.class);
	}
}
