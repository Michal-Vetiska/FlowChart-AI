package com.flowcart.commerce.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.flowcart.commerce.catalog.application.CreateProductCommand;
import com.flowcart.commerce.catalog.application.ProductService;
import com.flowcart.commerce.catalog.domain.Product;
import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.inventory.domain.Warehouse;
import com.flowcart.commerce.messaging.adapters.out.persistence.SpringDataOrderSagaJpaRepository;
import com.flowcart.commerce.messaging.domain.SagaState;
import com.flowcart.commerce.ordering.application.CreateOrderCommand;
import com.flowcart.commerce.ordering.application.OrderService;
import com.flowcart.commerce.ordering.domain.Order;
import com.flowcart.commerce.ordering.domain.OrderStatus;
import com.flowcart.commerce.support.PostgresTestcontainersConfig;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@Import(PostgresTestcontainersConfig.class)
@TestPropertySource(properties = {
		"flowcart.ordering.mode=saga",
		"flowcart.messaging.mode=in-memory",
		"flowcart.messaging.publish-after-commit=true"
})
class OrderSagaIntegrationTest {

	@Autowired
	private ProductService productService;
	@Autowired
	private InventoryService inventoryService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private SpringDataOrderSagaJpaRepository sagaRepository;

	@Test
	void sagaCompletesOrderOnSuccessfulPayment() {
		Product product = seedProduct("SAGA-OK");
		Warehouse warehouse = seedWarehouse("WSOK");
		inventoryService.createOrAdjustStock(warehouse.id(), product.id(), 5, "seed");

		Order created = orderService.createOrder(new CreateOrderCommand(
				"saga-ok@example.com",
				"Saga User",
				"CZK",
				null,
				"saga-ok-" + UUID.randomUUID(),
				List.of(new CreateOrderCommand.OrderLineCommand(product.id(), warehouse.id(), 1)),
				"SUCCESS"
		));

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			Order order = orderService.getById(created.id());
			assertThat(order.status()).isEqualTo(OrderStatus.CONFIRMED);
			assertThat(sagaRepository.findById(created.id())).isPresent();
			assertThat(sagaRepository.findById(created.id()).orElseThrow().getSagaState())
					.isEqualTo(SagaState.COMPLETED);
		});
	}

	@Test
	void sagaCompensatesOnPaymentFailure() {
		Product product = seedProduct("SAGA-FAIL");
		Warehouse warehouse = seedWarehouse("WSFL");
		inventoryService.createOrAdjustStock(warehouse.id(), product.id(), 5, "seed");

		Order created = orderService.createOrder(new CreateOrderCommand(
				"saga-fail@example.com",
				"Saga Fail",
				"CZK",
				null,
				"saga-fail-" + UUID.randomUUID(),
				List.of(new CreateOrderCommand.OrderLineCommand(product.id(), warehouse.id(), 1)),
				"DECLINED"
		));

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
			Order order = orderService.getById(created.id());
			assertThat(order.status()).isEqualTo(OrderStatus.CANCELLED);
			assertThat(inventoryService.findStock(warehouse.id(), product.id()).orElseThrow().availableQty())
					.isEqualTo(5);
		});
	}

	@Test
	void duplicateEventProcessingIsIdempotent() {
		Product product = seedProduct("SAGA-DUP");
		Warehouse warehouse = seedWarehouse("WSDP");
		inventoryService.createOrAdjustStock(warehouse.id(), product.id(), 3, "seed");

		Order created = orderService.createOrder(new CreateOrderCommand(
				"saga-dup@example.com",
				"Saga Dup",
				"CZK",
				null,
				"saga-dup-" + UUID.randomUUID(),
				List.of(new CreateOrderCommand.OrderLineCommand(product.id(), warehouse.id(), 1)),
				"SUCCESS"
		));

		await().atMost(Duration.ofSeconds(10)).untilAsserted(() ->
				assertThat(orderService.getById(created.id()).status()).isEqualTo(OrderStatus.CONFIRMED));

		// Second identical create with same idempotency key returns same order without breaking stock
		Order again = orderService.createOrder(new CreateOrderCommand(
				"saga-dup@example.com",
				"Saga Dup",
				"CZK",
				null,
				created.idempotencyKey(),
				List.of(new CreateOrderCommand.OrderLineCommand(product.id(), warehouse.id(), 1)),
				"SUCCESS"
		));
		assertThat(again.id()).isEqualTo(created.id());
		assertThat(inventoryService.findStock(warehouse.id(), product.id()).orElseThrow().availableQty())
				.isEqualTo(2);
	}

	private Product seedProduct(String prefix) {
		return productService.create(new CreateProductCommand(
				prefix + "-" + UUID.randomUUID().toString().substring(0, 6),
				prefix,
				"saga",
				new BigDecimal("40.00"),
				"CZK",
				null
		));
	}

	private Warehouse seedWarehouse(String codePrefix) {
		return inventoryService.createWarehouse(
				codePrefix + UUID.randomUUID().toString().substring(0, 4),
				codePrefix,
				"PRG"
		);
	}
}
