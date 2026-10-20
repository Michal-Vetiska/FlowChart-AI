package com.flowcart.commerce.chaos;

import com.flowcart.commerce.demo.ConcurrencyBurstService;
import com.flowcart.commerce.demo.ConcurrencyBurstService.ConcurrencyBurstResult;
import com.flowcart.commerce.payment.domain.SimulatedPaymentOutcome;
import com.flowcart.commerce.shared.domain.DomainException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ChaosService {

	private final boolean enabled;
	private final AtomicBoolean paymentForceDecline = new AtomicBoolean(false);
	private final AtomicLong inventoryDelayMs = new AtomicLong(0);
	private final AtomicBoolean duplicateEventMode = new AtomicBoolean(false);
	private final AtomicReference<String> activeScenario = new AtomicReference<>("NONE");
	private final Map<UUID, ChaosRun> runs = new ConcurrentHashMap<>();
	private final ConcurrencyBurstService concurrencyBurstService;

	public ChaosService(
			@Value("${flowcart.chaos.enabled:true}") boolean enabled,
			ConcurrencyBurstService concurrencyBurstService
	) {
		this.enabled = enabled;
		this.concurrencyBurstService = concurrencyBurstService;
	}

	public void assertEnabled() {
		if (!enabled) {
			throw new DomainException("Chaos Lab is disabled in this environment");
		}
	}

	public ChaosRun start(String scenarioType, Map<String, Object> params) {
		assertEnabled();
		UUID id = UUID.randomUUID();
		activeScenario.set(scenarioType);
		ChaosRun run = new ChaosRun(id, scenarioType, "RUNNING", params, null, Instant.now(), null);
		runs.put(id, run);

		return switch (scenarioType) {
			case "PAYMENT_FAILURE" -> {
				paymentForceDecline.set(true);
				yield finish(id, Map.of(
						"expected", "Next payment attempts decline and release stock",
						"observed", "paymentForceDecline=true"
				));
			}
			case "INVENTORY_TIMEOUT" -> {
				long delay = ((Number) params.getOrDefault("delayMs", 2000)).longValue();
				inventoryDelayMs.set(delay);
				yield finish(id, Map.of(
						"expected", "Inventory operations add artificial latency",
						"observed", "inventoryDelayMs=" + delay
				));
			}
			case "DUPLICATE_EVENT" -> {
				duplicateEventMode.set(true);
				yield finish(id, Map.of(
						"expected", "Consumers ignore already processed event ids",
						"observed", "duplicateEventMode=true"
				));
			}
			case "HIGH_CONCURRENCY" -> {
				UUID warehouseId = UUID.fromString(String.valueOf(params.get("warehouseId")));
				UUID productId = UUID.fromString(String.valueOf(params.get("productId")));
				int stock = ((Number) params.getOrDefault("stockQty", 10)).intValue();
				int requests = ((Number) params.getOrDefault("requests", 100)).intValue();
				ConcurrencyBurstResult result = concurrencyBurstService.runBurst(warehouseId, productId, stock, requests);
				yield finish(id, Map.of(
						"expected", "successfulReservations <= stockQty and consistent=true",
						"observed", result
				));
			}
			case "RESET" -> {
				reset();
				yield finish(id, Map.of("expected", "all chaos flags cleared", "observed", "reset"));
			}
			default -> throw new DomainException("Unknown chaos scenario: " + scenarioType);
		};
	}

	public void reset() {
		paymentForceDecline.set(false);
		inventoryDelayMs.set(0);
		duplicateEventMode.set(false);
		activeScenario.set("NONE");
	}

	public boolean shouldDeclinePayments() {
		return paymentForceDecline.get();
	}

	public long inventoryDelayMs() {
		return inventoryDelayMs.get();
	}

	public boolean duplicateEventMode() {
		return duplicateEventMode.get();
	}

	public ChaosStatus status() {
		return new ChaosStatus(
				enabled,
				activeScenario.get(),
				paymentForceDecline.get(),
				inventoryDelayMs.get(),
				duplicateEventMode.get(),
				runs.values().stream().toList()
		);
	}

	private ChaosRun finish(UUID id, Map<String, Object> result) {
		ChaosRun previous = runs.get(id);
		ChaosRun finished = new ChaosRun(
				previous.id(),
				previous.scenarioType(),
				"FINISHED",
				previous.parameters(),
				result,
				previous.startedAt(),
				Instant.now()
		);
		runs.put(id, finished);
		return finished;
	}

	public record ChaosRun(
			UUID id,
			String scenarioType,
			String status,
			Map<String, Object> parameters,
			Map<String, Object> result,
			Instant startedAt,
			Instant finishedAt
	) {
	}

	public record ChaosStatus(
			boolean enabled,
			String activeScenario,
			boolean paymentForceDecline,
			long inventoryDelayMs,
			boolean duplicateEventMode,
			java.util.List<ChaosRun> recentRuns
	) {
	}
}
