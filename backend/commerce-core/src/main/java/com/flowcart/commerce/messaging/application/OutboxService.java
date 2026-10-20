package com.flowcart.commerce.messaging.application;

import com.flowcart.commerce.messaging.adapters.out.persistence.OutboxMessageJpaEntity;
import com.flowcart.commerce.messaging.adapters.out.persistence.SpringDataOutboxJpaRepository;
import com.flowcart.commerce.messaging.domain.DomainEvent;
import com.flowcart.commerce.messaging.domain.OutboxStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
public class OutboxService {

	private final SpringDataOutboxJpaRepository outboxRepository;
	private final EventBus eventBus;
	private final boolean publishAfterCommit;

	public OutboxService(
			SpringDataOutboxJpaRepository outboxRepository,
			EventBus eventBus,
			@Value("${flowcart.messaging.publish-after-commit:true}") boolean publishAfterCommit
	) {
		this.outboxRepository = outboxRepository;
		this.eventBus = eventBus;
		this.publishAfterCommit = publishAfterCommit;
	}

	@Transactional
	public void enqueue(DomainEvent event) {
		OutboxMessageJpaEntity entity = new OutboxMessageJpaEntity();
		entity.setId(event.eventId());
		entity.setEventType(event.eventType());
		entity.setEventVersion(event.eventVersion());
		entity.setAggregateId(event.aggregateId());
		entity.setCorrelationId(event.correlationId());
		entity.setCausationId(event.causationId());
		entity.setPayload(event.payloadJson());
		entity.setStatus(OutboxStatus.PENDING);
		entity.setCreatedAt(event.occurredAt());
		entity.setAttempts(0);
		outboxRepository.save(entity);

		if (publishAfterCommit && TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					try {
						eventBus.publish(event);
						markPublished(entity.getId());
					}
					catch (Exception ex) {
						markFailed(entity.getId(), ex.getMessage());
					}
				}
			});
		}
	}

	@Transactional(readOnly = true)
	public List<OutboxMessageJpaEntity> findPending(int limit) {
		return outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.PENDING).stream()
				.limit(limit)
				.toList();
	}

	@Transactional
	public void markPublished(java.util.UUID id) {
		outboxRepository.findById(id).ifPresent(message -> {
			message.setStatus(OutboxStatus.PUBLISHED);
			message.setPublishedAt(Instant.now());
			outboxRepository.save(message);
		});
	}

	@Transactional
	public void markPublished(OutboxMessageJpaEntity message) {
		markPublished(message.getId());
	}

	@Transactional
	public void markFailed(java.util.UUID id, String error) {
		outboxRepository.findById(id).ifPresent(message -> {
			message.setAttempts(message.getAttempts() + 1);
			message.setLastError(error);
			if (message.getAttempts() >= 10) {
				message.setStatus(OutboxStatus.FAILED);
			}
			outboxRepository.save(message);
		});
	}

	@Transactional
	public void markFailed(OutboxMessageJpaEntity message, String error) {
		markFailed(message.getId(), error);
	}
}
