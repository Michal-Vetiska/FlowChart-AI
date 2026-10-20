package com.flowcart.commerce.messaging.application;

import com.flowcart.commerce.messaging.domain.DomainEvent;

public interface EventBus {

	void publish(DomainEvent event);
}
