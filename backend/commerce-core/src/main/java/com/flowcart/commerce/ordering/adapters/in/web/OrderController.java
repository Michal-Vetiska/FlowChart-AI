package com.flowcart.commerce.ordering.adapters.in.web;

import com.flowcart.commerce.ordering.adapters.in.web.OrderDtos.CancelOrderRequest;
import com.flowcart.commerce.ordering.adapters.in.web.OrderDtos.CreateOrderRequest;
import com.flowcart.commerce.ordering.adapters.in.web.OrderDtos.OrderResponse;
import com.flowcart.commerce.ordering.adapters.in.web.OrderDtos.PayOrderRequest;
import com.flowcart.commerce.ordering.application.CreateOrderCommand;
import com.flowcart.commerce.ordering.application.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	public OrderController(OrderService orderService) {
		this.orderService = orderService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrderResponse create(@Valid @RequestBody CreateOrderRequest request) {
		CreateOrderCommand command = new CreateOrderCommand(
				request.customerEmail(),
				request.customerFullName(),
				request.currency().toUpperCase(),
				request.couponCode(),
				request.idempotencyKey(),
				request.lines().stream()
						.map(line -> new CreateOrderCommand.OrderLineCommand(
								line.productId(),
								line.warehouseId(),
								line.quantity()
						))
						.toList()
		);
		return OrderResponse.from(orderService.createOrder(command));
	}

	@GetMapping
	public List<OrderResponse> list() {
		return orderService.list().stream().map(OrderResponse::from).toList();
	}

	@GetMapping("/{id}")
	public OrderResponse get(@PathVariable UUID id) {
		return OrderResponse.from(orderService.getById(id));
	}

	@PostMapping("/{id}/pay")
	public OrderResponse pay(@PathVariable UUID id, @Valid @RequestBody PayOrderRequest request) {
		return OrderResponse.from(orderService.pay(id, request.paymentIdempotencyKey(), request.outcome()));
	}

	@PostMapping("/{id}/cancel")
	public OrderResponse cancel(@PathVariable UUID id, @RequestBody(required = false) CancelOrderRequest request) {
		String reason = request == null ? null : request.reason();
		return OrderResponse.from(orderService.cancel(id, reason));
	}
}
