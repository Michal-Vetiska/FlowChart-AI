package com.flowcart.commerce.ordering.application;

import java.util.List;
import java.util.UUID;

public record CreateOrderCommand(
		String customerEmail,
		String customerFullName,
		String currency,
		String couponCode,
		String idempotencyKey,
		List<OrderLineCommand> lines,
		String paymentOutcome
) {
	public CreateOrderCommand(
			String customerEmail,
			String customerFullName,
			String currency,
			String couponCode,
			String idempotencyKey,
			List<OrderLineCommand> lines
	) {
		this(customerEmail, customerFullName, currency, couponCode, idempotencyKey, lines, "SUCCESS");
	}

	public record OrderLineCommand(UUID productId, UUID warehouseId, int quantity) {
	}
}
