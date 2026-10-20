package com.flowcart.commerce.ordering.adapters.in.web;

import com.flowcart.commerce.ordering.domain.Order;
import com.flowcart.commerce.ordering.domain.OrderItem;
import com.flowcart.commerce.ordering.domain.OrderStatusChange;
import com.flowcart.commerce.payment.domain.SimulatedPaymentOutcome;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class OrderDtos {

	private OrderDtos() {
	}

	public record CreateOrderRequest(
			@NotBlank String customerEmail,
			@NotBlank String customerFullName,
			@NotBlank @Size(min = 3, max = 3) String currency,
			String couponCode,
			String idempotencyKey,
			@NotEmpty @Valid List<OrderLineRequest> lines
	) {
	}

	public record OrderLineRequest(
			@NotNull UUID productId,
			@NotNull UUID warehouseId,
			@Min(1) int quantity
	) {
	}

	public record PayOrderRequest(
			@NotBlank String paymentIdempotencyKey,
			@NotNull SimulatedPaymentOutcome outcome
	) {
	}

	public record CancelOrderRequest(String reason) {
	}

	public record OrderResponse(
			UUID id,
			UUID customerId,
			String status,
			String currency,
			BigDecimal subtotalAmount,
			BigDecimal discountAmount,
			BigDecimal totalAmount,
			String couponCode,
			String idempotencyKey,
			List<OrderItemResponse> items,
			List<OrderStatusHistoryResponse> statusHistory,
			Instant createdAt,
			Instant updatedAt
	) {
		public static OrderResponse from(Order order) {
			return new OrderResponse(
					order.id(),
					order.customerId(),
					order.status().name(),
					order.currency(),
					order.subtotalAmount().amount(),
					order.discountAmount().amount(),
					order.totalAmount().amount(),
					order.couponCode(),
					order.idempotencyKey(),
					order.items().stream().map(OrderItemResponse::from).toList(),
					order.statusHistory().stream().map(OrderStatusHistoryResponse::from).toList(),
					order.createdAt(),
					order.updatedAt()
			);
		}
	}

	public record OrderItemResponse(
			UUID id,
			UUID productId,
			UUID warehouseId,
			String sku,
			String productName,
			int quantity,
			BigDecimal unitPrice,
			BigDecimal lineTotal
	) {
		public static OrderItemResponse from(OrderItem item) {
			return new OrderItemResponse(
					item.id(),
					item.productId(),
					item.warehouseId(),
					item.sku(),
					item.productName(),
					item.quantity(),
					item.unitPrice().amount(),
					item.lineTotal().amount()
			);
		}
	}

	public record OrderStatusHistoryResponse(
			UUID id,
			String fromStatus,
			String toStatus,
			String reason,
			Instant createdAt
	) {
		public static OrderStatusHistoryResponse from(OrderStatusChange change) {
			return new OrderStatusHistoryResponse(
					change.id(),
					change.fromStatus() == null ? null : change.fromStatus().name(),
					change.toStatus().name(),
					change.reason(),
					change.createdAt()
			);
		}
	}
}
