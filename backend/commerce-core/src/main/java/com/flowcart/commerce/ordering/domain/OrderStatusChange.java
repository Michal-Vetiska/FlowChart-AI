package com.flowcart.commerce.ordering.domain;

import java.time.Instant;
import java.util.UUID;

public record OrderStatusChange(
		UUID id,
		OrderStatus fromStatus,
		OrderStatus toStatus,
		String reason,
		Instant createdAt
) {
}
