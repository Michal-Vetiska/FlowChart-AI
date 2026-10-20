package com.flowcart.commerce.ordering.domain;

import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.Money;
import java.util.Objects;
import java.util.UUID;

public class OrderItem {

	private final UUID id;
	private final UUID productId;
	private final UUID warehouseId;
	private final String sku;
	private final String productName;
	private final int quantity;
	private final Money unitPrice;
	private final Money lineTotal;

	private OrderItem(
			UUID id,
			UUID productId,
			UUID warehouseId,
			String sku,
			String productName,
			int quantity,
			Money unitPrice,
			Money lineTotal
	) {
		this.id = Objects.requireNonNull(id);
		this.productId = Objects.requireNonNull(productId);
		this.warehouseId = Objects.requireNonNull(warehouseId);
		this.sku = Objects.requireNonNull(sku);
		this.productName = Objects.requireNonNull(productName);
		if (quantity <= 0) {
			throw new DomainException("Order item quantity must be positive");
		}
		this.quantity = quantity;
		this.unitPrice = Objects.requireNonNull(unitPrice);
		this.lineTotal = Objects.requireNonNull(lineTotal);
	}

	public static OrderItem create(
			UUID productId,
			UUID warehouseId,
			String sku,
			String productName,
			int quantity,
			Money unitPrice
	) {
		return new OrderItem(
				UUID.randomUUID(),
				productId,
				warehouseId,
				sku,
				productName,
				quantity,
				unitPrice,
				unitPrice.multiply(quantity)
		);
	}

	public static OrderItem rehydrate(
			UUID id,
			UUID productId,
			UUID warehouseId,
			String sku,
			String productName,
			int quantity,
			Money unitPrice,
			Money lineTotal
	) {
		return new OrderItem(id, productId, warehouseId, sku, productName, quantity, unitPrice, lineTotal);
	}

	public UUID id() {
		return id;
	}

	public UUID productId() {
		return productId;
	}

	public UUID warehouseId() {
		return warehouseId;
	}

	public String sku() {
		return sku;
	}

	public String productName() {
		return productName;
	}

	public int quantity() {
		return quantity;
	}

	public Money unitPrice() {
		return unitPrice;
	}

	public Money lineTotal() {
		return lineTotal;
	}
}
