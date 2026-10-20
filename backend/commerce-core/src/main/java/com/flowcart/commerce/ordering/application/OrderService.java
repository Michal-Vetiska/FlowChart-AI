package com.flowcart.commerce.ordering.application;

import com.flowcart.commerce.catalog.application.port.ProductRepository;
import com.flowcart.commerce.catalog.domain.Product;
import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.ordering.application.port.CustomerRepository;
import com.flowcart.commerce.ordering.application.port.OrderRepository;
import com.flowcart.commerce.ordering.domain.Customer;
import com.flowcart.commerce.ordering.domain.Order;
import com.flowcart.commerce.ordering.domain.OrderItem;
import com.flowcart.commerce.ordering.domain.OrderStatus;
import com.flowcart.commerce.payment.application.PaymentSimulator;
import com.flowcart.commerce.payment.application.port.PaymentRepository;
import com.flowcart.commerce.payment.domain.Payment;
import com.flowcart.commerce.payment.domain.PaymentStatus;
import com.flowcart.commerce.payment.domain.SimulatedPaymentOutcome;
import com.flowcart.commerce.messaging.application.OutboxService;
import com.flowcart.commerce.messaging.domain.DomainEvent;
import com.flowcart.commerce.messaging.application.OrderSagaOrchestrator;
import com.flowcart.commerce.pricing.application.PricingService;
import com.flowcart.commerce.shared.domain.ConflictException;
import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.Money;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

	private final OrderRepository orderRepository;
	private final CustomerRepository customerRepository;
	private final ProductRepository productRepository;
	private final InventoryService inventoryService;
	private final PricingService pricingService;
	private final PaymentSimulator paymentSimulator;
	private final PaymentRepository paymentRepository;
	private final ObjectProvider<OutboxService> outboxService;
	private final String orderingMode;

	public OrderService(
			OrderRepository orderRepository,
			CustomerRepository customerRepository,
			ProductRepository productRepository,
			InventoryService inventoryService,
			PricingService pricingService,
			PaymentSimulator paymentSimulator,
			PaymentRepository paymentRepository,
			ObjectProvider<OutboxService> outboxService,
			@Value("${flowcart.ordering.mode:sync}") String orderingMode
	) {
		this.orderRepository = orderRepository;
		this.customerRepository = customerRepository;
		this.productRepository = productRepository;
		this.inventoryService = inventoryService;
		this.pricingService = pricingService;
		this.paymentSimulator = paymentSimulator;
		this.paymentRepository = paymentRepository;
		this.outboxService = outboxService;
		this.orderingMode = orderingMode;
	}

	@Transactional
	public Order createOrder(CreateOrderCommand command) {
		Order order = persistNewOrder(command);
		if ("saga".equalsIgnoreCase(orderingMode)) {
			UUID correlationId = UUID.randomUUID();
			String paymentOutcome = command.paymentOutcome() == null ? "SUCCESS" : command.paymentOutcome();
			outboxService.getObject().enqueue(DomainEvent.of(
					OrderSagaOrchestrator.ORDER_CREATED,
					order.id(),
					correlationId,
					null,
					"{\"paymentOutcome\":\"" + paymentOutcome + "\"}"
			));
			return order;
		}

		order.transitionTo(OrderStatus.PENDING_RESERVATION, "Reserving stock");
		order = orderRepository.save(order);

		try {
			for (OrderItem item : order.items()) {
				inventoryService.reserve(item.warehouseId(), item.productId(), order.id(), item.quantity());
			}
			order.transitionTo(OrderStatus.STOCK_RESERVED, "Stock reserved");
			order.transitionTo(OrderStatus.PAYMENT_PENDING, "Awaiting payment");
			return orderRepository.save(order);
		}
		catch (DomainException ex) {
			inventoryService.releaseReservationsForOrder(order.id());
			order.transitionTo(OrderStatus.STOCK_UNAVAILABLE, ex.getMessage());
			return orderRepository.save(order);
		}
	}

	private Order persistNewOrder(CreateOrderCommand command) {
		if (command.idempotencyKey() != null && !command.idempotencyKey().isBlank()) {
			var existing = orderRepository.findByIdempotencyKey(command.idempotencyKey());
			if (existing.isPresent()) {
				return existing.get();
			}
		}
		if (command.lines() == null || command.lines().isEmpty()) {
			throw new DomainException("Order requires at least one line");
		}

		Customer customer = customerRepository.findByEmail(command.customerEmail())
				.orElseGet(() -> customerRepository.save(Customer.create(command.customerEmail(), command.customerFullName())));

		List<OrderItem> items = new ArrayList<>();
		Money subtotalProbe = Money.zero(command.currency());
		for (CreateOrderCommand.OrderLineCommand line : command.lines()) {
			Product product = productRepository.findById(line.productId())
					.orElseThrow(() -> new ResourceNotFoundException("Product not found: " + line.productId()));
			if (!product.active()) {
				throw new DomainException("Product is inactive: " + product.sku());
			}
			if (!product.basePrice().currencyCode().equals(command.currency())) {
				throw new DomainException("Product currency does not match order currency");
			}
			OrderItem item = OrderItem.create(
					product.id(),
					line.warehouseId(),
					product.sku(),
					product.name(),
					line.quantity(),
					product.basePrice()
			);
			items.add(item);
			subtotalProbe = subtotalProbe.add(item.lineTotal());
		}

		Money discount = pricingService.calculateDiscount(subtotalProbe, command.couponCode());
		Order order = Order.create(
				customer.id(),
				command.currency(),
				items,
				discount,
				command.couponCode(),
				command.idempotencyKey()
		);
		return orderRepository.save(order);
	}

	@Transactional
	public Order pay(UUID orderId, String paymentIdempotencyKey, SimulatedPaymentOutcome outcome) {
		Order order = getById(orderId);
		if (paymentIdempotencyKey == null || paymentIdempotencyKey.isBlank()) {
			throw new DomainException("paymentIdempotencyKey is required");
		}

		var existingPayment = paymentRepository.findByIdempotencyKey(paymentIdempotencyKey);
		if (existingPayment.isPresent()) {
			Payment payment = existingPayment.get();
			if (!payment.orderId().equals(orderId)) {
				throw new ConflictException("Idempotency key already used for another order");
			}
			return order;
		}

		if (order.status() != OrderStatus.PAYMENT_PENDING) {
			throw new ConflictException("Order is not awaiting payment: " + order.status());
		}

		Payment payment = paymentSimulator.charge(orderId, paymentIdempotencyKey, order.totalAmount(), outcome);
		paymentRepository.save(payment);

		if (payment.status() == PaymentStatus.AUTHORIZED) {
			order.transitionTo(OrderStatus.PAID, "Payment authorized");
			order.transitionTo(OrderStatus.CONFIRMED, "Order confirmed");
			inventoryService.consumeReservationsForOrder(orderId);
			return orderRepository.save(order);
		}

		order.transitionTo(OrderStatus.PAYMENT_FAILED, payment.failureReason());
		inventoryService.releaseReservationsForOrder(orderId);
		order.transitionTo(OrderStatus.CANCELLED, "Cancelled after payment failure");
		return orderRepository.save(order);
	}

	@Transactional
	public Order cancel(UUID orderId, String reason) {
		Order order = getById(orderId);
		if (order.status().isTerminal()) {
			throw new ConflictException("Cannot cancel terminal order: " + order.status());
		}
		if (order.status() == OrderStatus.STOCK_RESERVED || order.status() == OrderStatus.PAYMENT_PENDING) {
			inventoryService.releaseReservationsForOrder(orderId);
		}
		order.transitionTo(OrderStatus.CANCELLED, reason == null ? "Cancelled by request" : reason);
		return orderRepository.save(order);
	}

	@Transactional(readOnly = true)
	public Order getById(UUID id) {
		return orderRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
	}

	@Transactional(readOnly = true)
	public List<Order> list() {
		return orderRepository.findAll();
	}
}
