package com.flowcart.commerce.inventory.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Warehouse {

	private final UUID id;
	private String code;
	private String name;
	private String location;
	private boolean active;
	private final Instant createdAt;
	private Instant updatedAt;

	private Warehouse(
			UUID id,
			String code,
			String name,
			String location,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.code = requireCode(code);
		this.name = requireName(name);
		this.location = location;
		this.active = active;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static Warehouse create(String code, String name, String location) {
		Instant now = Instant.now();
		return new Warehouse(UUID.randomUUID(), code, name, location, true, now, now);
	}

	public static Warehouse rehydrate(
			UUID id,
			String code,
			String name,
			String location,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Warehouse(id, code, name, location, active, createdAt, updatedAt);
	}

	private static String requireCode(String code) {
		if (code == null || code.isBlank()) {
			throw new DomainException("Warehouse code is required");
		}
		return code.trim().toUpperCase();
	}

	private static String requireName(String name) {
		if (name == null || name.isBlank()) {
			throw new DomainException("Warehouse name is required");
		}
		return name.trim();
	}

	public UUID id() {
		return id;
	}

	public String code() {
		return code;
	}

	public String name() {
		return name;
	}

	public String location() {
		return location;
	}

	public boolean active() {
		return active;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
