package com.flowcart.commerce.ordering.adapters.out.persistence;

import com.flowcart.commerce.ordering.domain.Customer;
import com.flowcart.commerce.ordering.domain.Order;
import com.flowcart.commerce.ordering.domain.OrderItem;
import com.flowcart.commerce.ordering.domain.OrderStatusChange;
import com.flowcart.commerce.shared.domain.Money;
import java.util.ArrayList;
import java.util.List;

final class OrderingPersistenceMapper {

	private OrderingPersistenceMapper() {
	}

	static CustomerJpaEntity toEntity(Customer customer) {
		CustomerJpaEntity entity = new CustomerJpaEntity();
		entity.setId(customer.id());
		entity.setEmail(customer.email());
		entity.setFullName(customer.fullName());
		entity.setCreatedAt(customer.createdAt());
		entity.setUpdatedAt(customer.updatedAt());
		return entity;
	}

	static Customer toDomain(CustomerJpaEntity entity) {
		return Customer.rehydrate(
				entity.getId(),
				entity.getEmail(),
				entity.getFullName(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static OrderJpaEntity toEntity(Order order) {
		OrderJpaEntity entity = new OrderJpaEntity();
		entity.setId(order.id());
		entity.setCustomerId(order.customerId());
		entity.setStatus(order.status());
		entity.setCurrency(order.currency());
		entity.setSubtotalAmount(order.subtotalAmount().amount());
		entity.setDiscountAmount(order.discountAmount().amount());
		entity.setTotalAmount(order.totalAmount().amount());
		entity.setCouponCode(order.couponCode());
		entity.setIdempotencyKey(order.idempotencyKey());
		entity.setCreatedAt(order.createdAt());
		entity.setUpdatedAt(order.updatedAt());

		List<OrderItemJpaEntity> itemEntities = new ArrayList<>();
		for (OrderItem item : order.items()) {
			OrderItemJpaEntity itemEntity = new OrderItemJpaEntity();
			itemEntity.setId(item.id());
			itemEntity.setOrder(entity);
			itemEntity.setProductId(item.productId());
			itemEntity.setWarehouseId(item.warehouseId());
			itemEntity.setSku(item.sku());
			itemEntity.setProductName(item.productName());
			itemEntity.setQuantity(item.quantity());
			itemEntity.setUnitPrice(item.unitPrice().amount());
			itemEntity.setLineTotal(item.lineTotal().amount());
			itemEntities.add(itemEntity);
		}
		entity.setItems(itemEntities);

		List<OrderStatusHistoryJpaEntity> historyEntities = new ArrayList<>();
		for (OrderStatusChange change : order.statusHistory()) {
			OrderStatusHistoryJpaEntity historyEntity = new OrderStatusHistoryJpaEntity();
			historyEntity.setId(change.id());
			historyEntity.setOrder(entity);
			historyEntity.setFromStatus(change.fromStatus());
			historyEntity.setToStatus(change.toStatus());
			historyEntity.setReason(change.reason());
			historyEntity.setCreatedAt(change.createdAt());
			historyEntities.add(historyEntity);
		}
		entity.setStatusHistory(historyEntities);
		return entity;
	}

	static Order toDomain(OrderJpaEntity entity) {
		List<OrderItem> items = entity.getItems().stream()
				.map(item -> OrderItem.rehydrate(
						item.getId(),
						item.getProductId(),
						item.getWarehouseId(),
						item.getSku(),
						item.getProductName(),
						item.getQuantity(),
						Money.of(item.getUnitPrice(), entity.getCurrency()),
						Money.of(item.getLineTotal(), entity.getCurrency())
				))
				.toList();

		List<OrderStatusChange> history = entity.getStatusHistory().stream()
				.map(h -> new OrderStatusChange(
						h.getId(),
						h.getFromStatus(),
						h.getToStatus(),
						h.getReason(),
						h.getCreatedAt()
				))
				.toList();

		return Order.rehydrate(
				entity.getId(),
				entity.getCustomerId(),
				entity.getStatus(),
				entity.getCurrency(),
				Money.of(entity.getSubtotalAmount(), entity.getCurrency()),
				Money.of(entity.getDiscountAmount(), entity.getCurrency()),
				Money.of(entity.getTotalAmount(), entity.getCurrency()),
				entity.getCouponCode(),
				entity.getIdempotencyKey(),
				items,
				history,
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}
}
