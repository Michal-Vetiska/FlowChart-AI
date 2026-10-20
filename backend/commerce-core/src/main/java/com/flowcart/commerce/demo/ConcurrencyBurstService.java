package com.flowcart.commerce.demo;

import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.inventory.domain.StockItem;
import com.flowcart.commerce.shared.domain.DomainException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Service;

@Service
public class ConcurrencyBurstService {

	private final InventoryService inventoryService;

	public ConcurrencyBurstService(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	public ConcurrencyBurstResult runBurst(UUID warehouseId, UUID productId, int stockQty, int concurrentRequests) {
		if (stockQty < 0 || concurrentRequests <= 0) {
			throw new DomainException("stockQty must be >= 0 and concurrentRequests > 0");
		}

		inventoryService.createOrAdjustStock(warehouseId, productId, stockQty, "Concurrency burst seed");

		AtomicInteger successes = new AtomicInteger();
		AtomicInteger failures = new AtomicInteger();
		List<Long> latenciesMs = new ArrayList<>();

		try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
			List<Callable<Long>> tasks = new ArrayList<>();
			for (int i = 0; i < concurrentRequests; i++) {
				UUID orderId = UUID.randomUUID();
				tasks.add(() -> {
					long started = System.nanoTime();
					try {
						inventoryService.reserve(warehouseId, productId, orderId, 1);
						successes.incrementAndGet();
					}
					catch (DomainException ex) {
						failures.incrementAndGet();
					}
					return (System.nanoTime() - started) / 1_000_000L;
				});
			}

			List<Future<Long>> futures = executor.invokeAll(tasks);
			for (Future<Long> future : futures) {
				try {
					latenciesMs.add(future.get());
				}
				catch (ExecutionException ex) {
					failures.incrementAndGet();
				}
			}
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new DomainException("Concurrency burst interrupted", ex);
		}

		StockItem stock = inventoryService.findStock(warehouseId, productId)
				.orElseThrow(() -> new DomainException("Stock missing after burst"));

		boolean consistent = successes.get() <= stockQty
				&& stock.availableQty() >= 0
				&& stock.reservedQty() >= 0
				&& successes.get() == stock.reservedQty()
				&& stock.availableQty() + stock.reservedQty() == stockQty;

		latenciesMs.sort(Long::compareTo);
		long p95 = percentile(latenciesMs, 0.95);
		long p99 = percentile(latenciesMs, 0.99);
		double avg = latenciesMs.stream().mapToLong(Long::longValue).average().orElse(0);

		return new ConcurrencyBurstResult(
				concurrentRequests,
				stockQty,
				successes.get(),
				failures.get(),
				stock.availableQty(),
				stock.reservedQty(),
				consistent,
				avg,
				p95,
				p99
		);
	}

	private static long percentile(List<Long> sorted, double percentile) {
		if (sorted.isEmpty()) {
			return 0;
		}
		int index = Math.min(sorted.size() - 1, (int) Math.ceil(percentile * sorted.size()) - 1);
		return sorted.get(Math.max(index, 0));
	}

	public record ConcurrencyBurstResult(
			int requests,
			int initialStock,
			int successfulReservations,
			int failedReservations,
			int availableQty,
			int reservedQty,
			boolean consistent,
			double avgLatencyMs,
			long p95LatencyMs,
			long p99LatencyMs
	) {
	}
}
