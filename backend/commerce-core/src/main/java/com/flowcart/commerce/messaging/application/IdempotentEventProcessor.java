package com.flowcart.commerce.messaging.application;

import com.flowcart.commerce.messaging.adapters.out.persistence.ProcessedEventJpaEntity;
import com.flowcart.commerce.messaging.adapters.out.persistence.SpringDataProcessedEventJpaRepository;
import com.flowcart.commerce.messaging.domain.DomainEvent;
import java.time.Instant;
import java.util.function.Consumer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotentEventProcessor {

	private final SpringDataProcessedEventJpaRepository processedEventRepository;

	public IdempotentEventProcessor(SpringDataProcessedEventJpaRepository processedEventRepository) {
		this.processedEventRepository = processedEventRepository;
	}

	@Transactional
	public void processOnce(DomainEvent event, Consumer<DomainEvent> handler) {
		if (processedEventRepository.existsById(event.eventId())) {
			return;
		}
		handler.accept(event);
		processedEventRepository.save(new ProcessedEventJpaEntity(event.eventId(), event.eventType(), Instant.now()));
	}
}
