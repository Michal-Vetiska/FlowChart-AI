package com.flowcart.commerce.messaging.domain;

public enum SagaState {
	STARTED,
	AWAITING_STOCK,
	STOCK_RESERVED,
	AWAITING_PAYMENT,
	COMPLETED,
	COMPENSATING,
	FAILED
}
