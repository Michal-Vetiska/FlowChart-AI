package com.flowcart.commerce.messaging.application;

import com.flowcart.commerce.messaging.adapters.out.persistence.OutboxMessageJpaEntity;
import com.flowcart.commerce.messaging.domain.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxPublisher {

	private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);

	private final OutboxService outboxService;
	private final EventBus eventBus;

	public OutboxPublisher(OutboxService outboxService, EventBus eventBus) {
		this.outboxService = outboxService;
		this.eventBus = eventBus;
	}

	@Scheduled(fixedDelayString = "${flowcart.messaging.outbox-poll-ms:1000}")
	public void publishPending() {
		for (OutboxMessageJpaEntity message : outboxService.findPending(50)) {
			try {
				DomainEvent event = new DomainEvent(
						message.getId(),
						message.getEventType(),
						message.getEventVersion(),
						message.getCreatedAt(),
						message.getAggregateId(),
						message.getCorrelationId(),
						message.getCausationId(),
						message.getPayload()
				);
				eventBus.publish(event);
				outboxService.markPublished(message);
			}
			catch (Exception ex) {
				log.warn("Failed to publish outbox message {}: {}", message.getId(), ex.getMessage());
				outboxService.markFailed(message, ex.getMessage());
			}
		}
	}
}
