package com.flowcart.commerce.ai.incident;

import com.flowcart.commerce.ai.knowledge.KnowledgeService;
import com.flowcart.commerce.chaos.ChaosService;
import com.flowcart.commerce.ordering.application.port.OrderRepository;
import com.flowcart.commerce.ordering.domain.OrderStatus;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class IncidentInvestigatorService {

	private final OrderRepository orderRepository;
	private final ChaosService chaosService;
	private final KnowledgeService knowledgeService;

	public IncidentInvestigatorService(
			OrderRepository orderRepository,
			ChaosService chaosService,
			KnowledgeService knowledgeService
	) {
		this.orderRepository = orderRepository;
		this.chaosService = chaosService;
		this.knowledgeService = knowledgeService;
	}

	public IncidentReport investigate(String symptom) {
		List<String> evidence = new ArrayList<>();
		List<String> affected = new ArrayList<>();
		Map<String, Object> metrics = new LinkedHashMap<>();

		var orders = orderRepository.findAll();
		long cancelled = orders.stream().filter(o -> o.status() == OrderStatus.CANCELLED).count();
		long paymentFailed = orders.stream().filter(o -> o.status() == OrderStatus.PAYMENT_FAILED).count();
		metrics.put("cancelledOrders", cancelled);
		metrics.put("paymentFailedOrders", paymentFailed);
		metrics.put("totalOrders", orders.size());
		evidence.add("Queried order repository for failure counts");

		var chaos = chaosService.status();
		metrics.put("chaosActiveScenario", chaos.activeScenario());
		metrics.put("paymentForceDecline", chaos.paymentForceDecline());
		metrics.put("inventoryDelayMs", chaos.inventoryDelayMs());
		evidence.add("Read Chaos Lab flags (activeScenario=" + chaos.activeScenario() + ")");

		if (chaos.paymentForceDecline()) {
			affected.add("payment");
			affected.add("ordering");
		}
		if (chaos.inventoryDelayMs() > 0) {
			affected.add("inventory");
		}
		if (affected.isEmpty()) {
			affected.add("ordering");
		}

		var rag = knowledgeService.query(
				symptom == null || symptom.isBlank()
						? "order processing payment failure inventory timeout"
						: symptom,
				3
		);
		if (!rag.insufficientEvidence()) {
			evidence.add("Retrieved incident/runbook evidence with " + rag.citations().size() + " citation(s)");
		}
		else {
			evidence.add("No indexed incident docs matched; relying on live telemetry only");
		}

		String suspected;
		String confidence;
		List<String> remediation = new ArrayList<>();

		if (chaos.paymentForceDecline()) {
			suspected = "Chaos Lab payment-failure injection is active, causing payment declines and order cancellations.";
			confidence = "high";
			remediation.add("Call POST /api/v1/chaos/reset");
			remediation.add("Re-run a successful payment scenario and confirm order reaches CONFIRMED");
		}
		else if (chaos.inventoryDelayMs() > 0) {
			suspected = "Artificial inventory latency is enabled (" + chaos.inventoryDelayMs() + " ms).";
			confidence = "high";
			remediation.add("Reset Chaos Lab inventory delay");
			remediation.add("Inspect traces for inventory span duration");
		}
		else if (cancelled > 0 || paymentFailed > 0) {
			suspected = "Elevated cancelled/payment-failed orders observed, but no active chaos flag. Investigate payment and stock reservation logs.";
			confidence = "medium";
			remediation.add("Check /api/v1/orders recent status history");
			remediation.add("Inspect Grafana/Tempo for payment and inventory spans");
		}
		else {
			suspected = "No strong failure signal in current telemetry.";
			confidence = "low";
			remediation.add("Reproduce with Chaos Lab scenario PAYMENT_FAILURE or INVENTORY_TIMEOUT");
		}

		return new IncidentReport(
				"INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
				Instant.now(),
				affected,
				List.of(symptom == null ? "Unspecified operational incident" : symptom),
				metrics,
				evidence,
				suspected,
				confidence,
				remediation,
				rag.citations().stream()
						.map(c -> Map.<String, Object>of(
								"documentId", c.documentId().toString(),
								"title", c.documentTitle(),
								"ref", c.ref()
						))
						.toList()
		);
	}

	public record IncidentReport(
			String incidentId,
			Instant investigatedAt,
			List<String> affectedServices,
			List<String> observedSymptoms,
			Map<String, Object> metrics,
			List<String> evidence,
			String suspectedRootCause,
			String confidence,
			List<String> recommendedRemediation,
			List<Map<String, Object>> knowledgeHits
	) {
	}
}
