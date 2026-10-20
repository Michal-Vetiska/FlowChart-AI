package com.flowcart.commerce.pricing.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public class DiscountCampaign {

	private final UUID id;
	private final String code;
	private final String name;
	private final DiscountType discountType;
	private final BigDecimal discountValue;
	private final Instant validFrom;
	private final Instant validTo;
	private final boolean active;
	private final Instant createdAt;
	private final Instant updatedAt;

	private DiscountCampaign(
			UUID id,
			String code,
			String name,
			DiscountType discountType,
			BigDecimal discountValue,
			Instant validFrom,
			Instant validTo,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		this.id = Objects.requireNonNull(id);
		this.code = requireCode(code);
		this.name = Objects.requireNonNull(name);
		this.discountType = Objects.requireNonNull(discountType);
		this.discountValue = Objects.requireNonNull(discountValue);
		if (discountValue.signum() <= 0) {
			throw new DomainException("Discount value must be positive");
		}
		this.validFrom = Objects.requireNonNull(validFrom);
		this.validTo = Objects.requireNonNull(validTo);
		if (!validTo.isAfter(validFrom)) {
			throw new DomainException("validTo must be after validFrom");
		}
		this.active = active;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static DiscountCampaign create(
			String code,
			String name,
			DiscountType discountType,
			BigDecimal discountValue,
			Instant validFrom,
			Instant validTo
	) {
		Instant now = Instant.now();
		return new DiscountCampaign(UUID.randomUUID(), code, name, discountType, discountValue, validFrom, validTo, true, now, now);
	}

	public static DiscountCampaign rehydrate(
			UUID id,
			String code,
			String name,
			DiscountType discountType,
			BigDecimal discountValue,
			Instant validFrom,
			Instant validTo,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		return new DiscountCampaign(id, code, name, discountType, discountValue, validFrom, validTo, active, createdAt, updatedAt);
	}

	public boolean isValidAt(Instant moment) {
		return active && !moment.isBefore(validFrom) && !moment.isAfter(validTo);
	}

	private static String requireCode(String code) {
		if (code == null || code.isBlank()) {
			throw new DomainException("Coupon code is required");
		}
		return code.trim().toUpperCase();
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

	public DiscountType discountType() {
		return discountType;
	}

	public BigDecimal discountValue() {
		return discountValue;
	}

	public Instant validFrom() {
		return validFrom;
	}

	public Instant validTo() {
		return validTo;
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
