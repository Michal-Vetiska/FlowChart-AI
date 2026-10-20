package com.flowcart.commerce.messaging.application;

import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.messaging.adapters.out.persistence.OrderSagaJpaEntity;
import com.flowcart.commerce.messaging.adapters.out.persistence.SpringDataOrderSagaJpaRepository;
import com.flowcart.commerce.messaging.domain.DomainEvent;
import com.flowcart.commerce.messaging.domain.SagaState;
import com.flowcart.commerce.ordering.application.port.OrderRepository;
import com.flowcart.commerce.ordering.domain.Order;
import com.flowcart.commerce.ordering.domain.OrderItem;
import com.flowcart.commerce.ordering.domain.OrderStatus;
import com.flowcart.commerce.payment.application.PaymentSimulator;
import com.flowcart.commerce.payment.application.port.PaymentRepository;
import com.flowcart.commerce.payment.domain.Payment;
import com.flowcart.commerce.payment.domain.PaymentStatus;
import com.flowcart.commerce.payment.domain.SimulatedPaymentOutcome;
import com.flowcart.commerce.shared.domain.DomainException;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(name = "flowcart.ordering.mode", havingValue = "saga")
public class OrderSagaOrchestrator {

	public static final String ORDER_CREATED = "OrderCreated";
	public static final String STOCK_RESERVED = "StockReserved";
	public static final String STOCK_RESERVATION_FAILED = "StockReservationFailed";
	public static final String PAYMENT_AUTHORIZED = "PaymentAuthorized";
	public static final String PAYMENT_REJECTED = "PaymentRejected";
	public static final String ORDER_CONFIRMED = "OrderConfirmed";
	public static final String ORDER_CANCELLED = "OrderCancelled";

	private static final Logger log = LoggerFactory.getLogger(OrderSagaOrchestrator.class);

	private final InMemoryEventBus eventBus;
	private final IdempotentEventProcessor idempotentEventProcessor;
	private final SpringDataOrderSagaJpaRepository sagaRepository;
	private final OrderRepository orderRepository;
	private final InventoryService inventoryService;
	private final PaymentSimulator paymentSimulator;
	private final PaymentRepository paymentRepository;
	private final OutboxService outboxService;

	public OrderSagaOrchestrator(
			InMemoryEventBus eventBus,
			IdempotentEventProcessor idempotentEventProcessor,
			SpringDataOrderSagaJpaRepository sagaRepository,
			OrderRepository orderRepository,
			InventoryService inventoryService,
			PaymentSimulator paymentSimulator,
			PaymentRepository paymentRepository,
			OutboxService outboxService
	) {
		this.eventBus = eventBus;
		this.idempotentEventProcessor = idempotentEventProcessor;
		this.sagaRepository = sagaRepository;
		this.orderRepository = orderRepository;
		this.inventoryService = inventoryService;
		this.paymentSimulator = paymentSimulator;
		this.paymentRepository = paymentRepository;
		this.outboxService = outboxService;
	}

	@PostConstruct
	void subscribe() {
		eventBus.subscribe(event -> idempotentEventProcessor.processOnce(event, this::handle));
	}

	@Transactional
	protected void handle(DomainEvent event) {
		switch (event.eventType()) {
			case ORDER_CREATED -> onOrderCreated(event);
			case STOCK_RESERVED -> onStockReserved(event);
			case STOCK_RESERVATION_FAILED -> onStockFailed(event);
			case PAYMENT_AUTHORIZED -> onPaymentAuthorized(event);
			case PAYMENT_REJECTED -> onPaymentRejected(event);
			default -> log.debug("Ignoring event type {}", event.eventType());
		}
	}

	private void onOrderCreated(DomainEvent event) {
		Order order = orderRepository.findById(event.aggregateId())
				.orElseThrow(() -> new DomainException("Order not found for saga: " + event.aggregateId()));
		String paymentOutcome = readPaymentOutcome(event.payloadJson());
		saveSaga(order.id(), event.correlationId(), SagaState.AWAITING_STOCK, paymentOutcome, null);

		try {
			order.transitionTo(OrderStatus.PENDING_RESERVATION, "Saga reserving stock");
			for (OrderItem item : order.items()) {
				inventoryService.reserve(item.warehouseId(), item.productId(), order.id(), item.quantity());
			}
			order.transitionTo(OrderStatus.STOCK_RESERVED, "Stock reserved by saga");
			orderRepository.save(order);
			saveSaga(order.id(), event.correlationId(), SagaState.STOCK_RESERVED, paymentOutcome, null);
			enqueue(STOCK_RESERVED, order.id(), event.correlationId(), event.eventId(), "{}");
		}
		catch (DomainException ex) {
			order.transitionTo(OrderStatus.STOCK_UNAVAILABLE, ex.getMessage());
			orderRepository.save(order);
			saveSaga(order.id(), event.correlationId(), SagaState.FAILED, paymentOutcome, ex.getMessage());
			enqueue(STOCK_RESERVATION_FAILED, order.id(), event.correlationId(), event.eventId(),
					"{\"error\":\"" + sanitize(ex.getMessage()) + "\"}");
		}
	}

	private void onStockReserved(DomainEvent event) {
		Order order = orderRepository.findById(event.aggregateId()).orElseThrow();
		OrderSagaJpaEntity saga = sagaRepository.findById(order.id()).orElseThrow();
		order.transitionTo(OrderStatus.PAYMENT_PENDING, "Saga awaiting payment");
		orderRepository.save(order);
		saveSaga(order.id(), event.correlationId(), SagaState.AWAITING_PAYMENT, saga.getPaymentOutcome(), null);

		SimulatedPaymentOutcome outcome = SimulatedPaymentOutcome.valueOf(
				saga.getPaymentOutcome() == null ? "SUCCESS" : saga.getPaymentOutcome());
		String paymentKey = "saga-pay-" + order.id();
		Payment payment = paymentSimulator.charge(order.id(), paymentKey, order.totalAmount(), outcome);
		paymentRepository.save(payment);

		if (payment.status() == PaymentStatus.AUTHORIZED) {
			enqueue(PAYMENT_AUTHORIZED, order.id(), event.correlationId(), event.eventId(), "{\"status\":\"AUTHORIZED\"}");
		}
		else {
			enqueue(PAYMENT_REJECTED, order.id(), event.correlationId(), event.eventId(),
					"{\"status\":\"" + payment.status() + "\"}");
		}
	}

	private void onPaymentAuthorized(DomainEvent event) {
		Order order = orderRepository.findById(event.aggregateId()).orElseThrow();
		order.transitionTo(OrderStatus.PAID, "Payment authorized by saga");
		order.transitionTo(OrderStatus.CONFIRMED, "Order confirmed by saga");
		inventoryService.consumeReservationsForOrder(order.id());
		orderRepository.save(order);
		saveSaga(order.id(), event.correlationId(), SagaState.COMPLETED, "SUCCESS", null);
		enqueue(ORDER_CONFIRMED, order.id(), event.correlationId(), event.eventId(), "{\"status\":\"CONFIRMED\"}");
	}

	private void onPaymentRejected(DomainEvent event) {
		Order order = orderRepository.findById(event.aggregateId()).orElseThrow();
		order.transitionTo(OrderStatus.PAYMENT_FAILED, "Payment rejected by saga");
		inventoryService.releaseReservationsForOrder(order.id());
		order.transitionTo(OrderStatus.CANCELLED, "Compensated after payment failure");
		orderRepository.save(order);
		saveSaga(order.id(), event.correlationId(), SagaState.FAILED, "REJECTED", "payment rejected");
		enqueue(ORDER_CANCELLED, order.id(), event.correlationId(), event.eventId(), "{\"status\":\"CANCELLED\"}");
	}

	private void onStockFailed(DomainEvent event) {
		saveSaga(event.aggregateId(), event.correlationId(), SagaState.FAILED, null, "stock unavailable");
	}

	private void enqueue(String type, UUID aggregateId, UUID correlationId, UUID causationId, String payload) {
		outboxService.enqueue(DomainEvent.of(type, aggregateId, correlationId, causationId, payload));
	}

	private void saveSaga(UUID orderId, UUID correlationId, SagaState state, String paymentOutcome, String error) {
		OrderSagaJpaEntity saga = sagaRepository.findById(orderId).orElseGet(OrderSagaJpaEntity::new);
		if (saga.getOrderId() == null) {
			saga.setOrderId(orderId);
			saga.setCreatedAt(Instant.now());
			saga.setCorrelationId(correlationId);
		}
		saga.setSagaState(state);
		if (paymentOutcome != null) {
			saga.setPaymentOutcome(paymentOutcome);
		}
		saga.setLastError(error);
		saga.setUpdatedAt(Instant.now());
		sagaRepository.save(saga);
	}

	private String readPaymentOutcome(String payloadJson) {
		if (payloadJson == null || payloadJson.isBlank()) {
			return "SUCCESS";
		}
		String marker = "\"paymentOutcome\":\"";
		int start = payloadJson.indexOf(marker);
		if (start < 0) {
			return "SUCCESS";
		}
		int valueStart = start + marker.length();
		int valueEnd = payloadJson.indexOf('"', valueStart);
		if (valueEnd < 0) {
			return "SUCCESS";
		}
		return payloadJson.substring(valueStart, valueEnd);
	}

	private static String sanitize(String value) {
		return value == null ? "" : value.replace("\"", "'");
	}
}
