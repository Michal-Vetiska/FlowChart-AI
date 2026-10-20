package com.flowcart.commerce.catalog.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

public class Category {

	private final UUID id;
	private String name;
	private String slug;
	private String description;
	private final Instant createdAt;
	private Instant updatedAt;

	private Category(UUID id, String name, String slug, String description, Instant createdAt, Instant updatedAt) {
		this.id = Objects.requireNonNull(id);
		this.name = requireName(name);
		this.slug = requireSlug(slug);
		this.description = description;
		this.createdAt = Objects.requireNonNull(createdAt);
		this.updatedAt = Objects.requireNonNull(updatedAt);
	}

	public static Category create(String name, String description) {
		Instant now = Instant.now();
		String slug = toSlug(name);
		return new Category(UUID.randomUUID(), name, slug, description, now, now);
	}

	public static Category rehydrate(
			UUID id,
			String name,
			String slug,
			String description,
			Instant createdAt,
			Instant updatedAt
	) {
		return new Category(id, name, slug, description, createdAt, updatedAt);
	}

	public void rename(String name) {
		this.name = requireName(name);
		this.slug = toSlug(name);
		this.updatedAt = Instant.now();
	}

	public void updateDescription(String description) {
		this.description = description;
		this.updatedAt = Instant.now();
	}

	private static String toSlug(String name) {
		return name.trim().toLowerCase(Locale.ROOT)
				.replaceAll("[^a-z0-9]+", "-")
				.replaceAll("(^-|-$)", "");
	}

	private static String requireName(String name) {
		if (name == null || name.isBlank()) {
			throw new DomainException("Category name is required");
		}
		return name.trim();
	}

	private static String requireSlug(String slug) {
		if (slug == null || slug.isBlank()) {
			throw new DomainException("Category slug is required");
		}
		return slug;
	}

	public UUID id() {
		return id;
	}

	public String name() {
		return name;
	}

	public String slug() {
		return slug;
	}

	public String description() {
		return description;
	}

	public Instant createdAt() {
		return createdAt;
	}

	public Instant updatedAt() {
		return updatedAt;
	}
}
