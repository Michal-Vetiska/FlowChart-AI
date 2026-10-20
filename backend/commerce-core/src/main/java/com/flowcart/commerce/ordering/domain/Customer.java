package com.flowcart.commerce.ordering.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Customer {

	private final UUID id;
	private final String email;
	private String fullName;
	private final Instant createdAt;
	private Instant updatedAt;

	private Customer(UUID id, String email, String fullName, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id);
		this.email = requireEmail(email);
		this.fullName = requireName(fullName);
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static Customer create(String email, String fullName) {
		Instant now = Instant.now();
		return new Customer(UUID.randomUUID(), email, fullName, now, now);
	}

	public static Customer rehydrate(UUID id, String email, String fullName, Instant createdAt, Instant updatedAt) {
		return new Customer(id, email, fullName, createdAt, updatedAt);
	}

	private static String requireEmail(String email) {
		if (email == null || email.isBlank() || !email.contains("@")) {
			throw new DomainException("Valid customer email is required");
		}
		return email.trim().toLowerCase();
	}

	private static String requireName(String fullName) {
		if (fullName == null || fullName.isBlank()) {
			throw new DomainException("Customer full name is required");
		}
		return fullName.trim();
	}

	public UUID id() {
		return id;
	}

	public String email() {
		return email;
	}

	public String fullName() {
		return fullName;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
