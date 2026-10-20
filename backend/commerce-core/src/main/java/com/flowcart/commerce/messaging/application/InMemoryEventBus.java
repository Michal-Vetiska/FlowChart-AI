package com.flowcart.commerce.messaging.application;

import com.flowcart.commerce.messaging.domain.DomainEvent;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "flowcart.messaging.mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryEventBus implements EventBus {

	private final List<Consumer<DomainEvent>> listeners = new CopyOnWriteArrayList<>();

	public void subscribe(Consumer<DomainEvent> listener) {
		listeners.add(listener);
	}

	@Override
	public void publish(DomainEvent event) {
		for (Consumer<DomainEvent> listener : listeners) {
			listener.accept(event);
		}
	}
}
