package com.flowcart.commerce.inventory;

import static org.assertj.core.api.Assertions.assertThat;

import com.flowcart.commerce.catalog.application.CreateProductCommand;
import com.flowcart.commerce.catalog.application.ProductService;
import com.flowcart.commerce.catalog.domain.Product;
import com.flowcart.commerce.demo.ConcurrencyBurstService;
import com.flowcart.commerce.demo.ConcurrencyBurstService.ConcurrencyBurstResult;
import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.inventory.domain.StockItem;
import com.flowcart.commerce.inventory.domain.StockReservation;
import com.flowcart.commerce.inventory.domain.Warehouse;
import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.support.PostgresTestcontainersConfig;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(PostgresTestcontainersConfig.class)
class ConcurrentReservationIntegrationTest {

	@Autowired
	private ProductService productService;

	@Autowired
	private InventoryService inventoryService;

	@Autowired
	private ConcurrencyBurstService concurrencyBurstService;

	@Test
	void oneHundredConcurrentReservationsNeverOversellTenUnits() {
		Product product = productService.create(new CreateProductCommand(
				"CONC-" + UUID.randomUUID().toString().substring(0, 8),
				"Concurrent Widget",
				"test",
				new BigDecimal("50.00"),
				"CZK",
				null
		));
		Warehouse warehouse = inventoryService.createWarehouse(
				"WHC-" + UUID.randomUUID().toString().substring(0, 6),
				"Concurrency WH",
				"PRG"
		);

		ConcurrencyBurstResult result = concurrencyBurstService.runBurst(
				warehouse.id(),
				product.id(),
				10,
				100
		);

		assertThat(result.successfulReservations()).isLessThanOrEqualTo(10);
		assertThat(result.successfulReservations()).isEqualTo(10);
		assertThat(result.failedReservations()).isEqualTo(90);
		assertThat(result.availableQty()).isGreaterThanOrEqualTo(0);
		assertThat(result.reservedQty()).isEqualTo(10);
		assertThat(result.consistent()).isTrue();
	}

	@Test
	void concurrentCancellationsReleaseStockSafely() throws Exception {
		Product product = productService.create(new CreateProductCommand(
				"CANC-" + UUID.randomUUID().toString().substring(0, 8),
				"Cancel Widget",
				"test",
				new BigDecimal("20.00"),
				"CZK",
				null
		));
		Warehouse warehouse = inventoryService.createWarehouse(
				"WHX-" + UUID.randomUUID().toString().substring(0, 6),
				"Cancel WH",
				"PRG"
		);
		inventoryService.createOrAdjustStock(warehouse.id(), product.id(), 5, "seed");

		UUID orderA = UUID.randomUUID();
		UUID orderB = UUID.randomUUID();
		inventoryService.reserve(warehouse.id(), product.id(), orderA, 2);
		inventoryService.reserve(warehouse.id(), product.id(), orderB, 2);

		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger releases = new AtomicInteger();
		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			executor.submit(() -> {
				start.await();
				inventoryService.releaseReservationsForOrder(orderA);
				releases.incrementAndGet();
				return null;
			});
			executor.submit(() -> {
				start.await();
				inventoryService.releaseReservationsForOrder(orderB);
				releases.incrementAndGet();
				return null;
			});
			start.countDown();
			executor.shutdown();
			assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();
		}

		assertThat(releases.get()).isEqualTo(2);
		StockItem stock = inventoryService.findStock(warehouse.id(), product.id()).orElseThrow();
		assertThat(stock.availableQty()).isEqualTo(5);
		assertThat(stock.reservedQty()).isEqualTo(0);
	}

	@Test
	void expiredReservationIsReleasedByJob() {
		Product product = productService.create(new CreateProductCommand(
				"EXP-" + UUID.randomUUID().toString().substring(0, 8),
				"Expire Widget",
				"test",
				new BigDecimal("10.00"),
				"CZK",
				null
		));
		Warehouse warehouse = inventoryService.createWarehouse(
				"WHE-" + UUID.randomUUID().toString().substring(0, 6),
				"Expire WH",
				"PRG"
		);
		inventoryService.createOrAdjustStock(warehouse.id(), product.id(), 3, "seed");

		StockReservation reservation = inventoryService.reserveWithTtl(
				warehouse.id(),
				product.id(),
				UUID.randomUUID(),
				1,
				Duration.ofMillis(1)
		);
		assertThat(reservation.status().name()).isEqualTo("ACTIVE");

		try {
			Thread.sleep(20);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new DomainException("interrupted", e);
		}

		int expired = inventoryService.expireDueReservations();
		assertThat(expired).isGreaterThanOrEqualTo(1);

		StockItem stock = inventoryService.findStock(warehouse.id(), product.id()).orElseThrow();
		assertThat(stock.availableQty()).isEqualTo(3);
		assertThat(stock.reservedQty()).isEqualTo(0);
	}
}
