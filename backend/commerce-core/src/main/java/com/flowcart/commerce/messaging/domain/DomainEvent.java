package com.flowcart.commerce.messaging.domain;

import java.time.Instant;
import java.util.UUID;

public record DomainEvent(
		UUID eventId,
		String eventType,
		int eventVersion,
		Instant occurredAt,
		UUID aggregateId,
		UUID correlationId,
		UUID causationId,
		String payloadJson
) {
	public static DomainEvent of(
			String eventType,
			UUID aggregateId,
			UUID correlationId,
			UUID causationId,
			String payloadJson
	) {
		return new DomainEvent(
				UUID.randomUUID(),
				eventType,
				1,
				Instant.now(),
				aggregateId,
				correlationId,
				causationId,
				payloadJson
		);
	}
}
