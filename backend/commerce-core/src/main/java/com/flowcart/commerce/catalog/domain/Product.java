package com.flowcart.commerce.catalog.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.Money;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class Product {

	private final UUID id;
	private String sku;
	private String name;
	private String description;
	private Money basePrice;
	private boolean active;
	private UUID categoryId;
	private final Instant createdAt;
	private Instant updatedAt;

	private Product(
			UUID id,
			String sku,
			String name,
			String description,
			Money basePrice,
			boolean active,
			UUID categoryId,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.sku = requireSku(sku);
		this.name = requireName(name);
		this.description = description;
		this.basePrice = requirePositivePrice(basePrice);
		this.active = active;
		this.categoryId = categoryId;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static Product create(String sku, String name, String description, Money basePrice, UUID categoryId) {
		Instant now = Instant.now();
		return new Product(UUID.randomUUID(), sku, name, description, basePrice, true, categoryId, now, now);
	}

	public static Product rehydrate(
			UUID id,
			String sku,
			String name,
			String description,
			Money basePrice,
			boolean active,
			UUID categoryId,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Product(id, sku, name, description, basePrice, active, categoryId, createdAt, updatedAt);
	}

	public void rename(String name) {
		this.name = requireName(name);
		touch();
	}

	public void updateDescription(String description) {
		this.description = description;
		touch();
	}

	public void changePrice(Money basePrice) {
		this.basePrice = requirePositivePrice(basePrice);
		touch();
	}

	public void assignCategory(UUID categoryId) {
		this.categoryId = categoryId;
		touch();
	}

	public void activate() {
		this.active = true;
		touch();
	}

	public void deactivate() {
		this.active = false;
		touch();
	}

	private void touch() {
		this.updatedAt = Instant.now();
	}

	private static String requireSku(String sku) {
		if (sku == null || sku.isBlank()) {
			throw new DomainException("SKU is required");
		}
		String normalized = sku.trim().toUpperCase();
		if (!normalized.matches("[A-Z0-9-]{3,64}")) {
			throw new DomainException("SKU must be 3-64 chars of A-Z, 0-9, or '-'");
		}
		return normalized;
	}

	private static String requireName(String name) {
		if (name == null || name.isBlank()) {
			throw new DomainException("Product name is required");
		}
		String trimmed = name.trim();
		if (trimmed.length() > 200) {
			throw new DomainException("Product name must be <= 200 characters");
		}
		return trimmed;
	}

	private static Money requirePositivePrice(Money basePrice) {
		Objects.requireNonNull(basePrice, "basePrice");
		if (basePrice.isZero() || basePrice.isNegative()) {
			throw new DomainException("Base price must be greater than zero");
		}
		return basePrice;
	}

	public UUID id() {
		return id;
	}

	public String sku() {
		return sku;
	}

	public String name() {
		return name;
	}

	public String description() {
		return description;
	}

	public Money basePrice() {
		return basePrice;
	}

	public boolean active() {
		return active;
	}

	public UUID categoryId() {
		return categoryId;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
