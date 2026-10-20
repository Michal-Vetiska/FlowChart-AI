package com.flowcart.commerce.shared.domain;

public class InvalidStateTransitionException extends DomainException {

	public InvalidStateTransitionException(String message) {
		super(message);
	}
}
