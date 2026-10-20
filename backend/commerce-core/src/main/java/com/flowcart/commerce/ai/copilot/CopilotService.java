package com.flowcart.commerce.ai.copilot;

import com.flowcart.commerce.ai.knowledge.KnowledgeService;
import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.ordering.application.port.OrderRepository;
import com.flowcart.commerce.ordering.domain.Order;
import com.flowcart.commerce.ordering.domain.OrderStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CopilotService {

	private final OrderRepository orderRepository;
	private final InventoryService inventoryService;
	private final KnowledgeService knowledgeService;

	public CopilotService(
			OrderRepository orderRepository,
			InventoryService inventoryService,
			KnowledgeService knowledgeService
	) {
		this.orderRepository = orderRepository;
		this.inventoryService = inventoryService;
		this.knowledgeService = knowledgeService;
	}

	@Transactional(readOnly = true)
	public CopilotResponse ask(String question) {
		List<ToolTrace> tools = new ArrayList<>();
		Map<String, Object> metrics = new LinkedHashMap<>();
		String q = question == null ? "" : question.toLowerCase(Locale.ROOT);

		if (q.contains("cancel") || q.contains("fail") || q.contains("order")) {
			List<Order> orders = orderRepository.findAll();
			long cancelled = orders.stream().filter(o -> o.status() == OrderStatus.CANCELLED).count();
			long paymentFailed = orders.stream().filter(o -> o.status() == OrderStatus.PAYMENT_FAILED).count();
			long confirmed = orders.stream().filter(o -> o.status() == OrderStatus.CONFIRMED).count();
			metrics.put("totalOrders", orders.size());
			metrics.put("cancelledOrders", cancelled);
			metrics.put("paymentFailedOrders", paymentFailed);
			metrics.put("confirmedOrders", confirmed);
			tools.add(new ToolTrace("getOrderStatistics", Map.of(), metrics));
		}

		if (q.contains("inventory") || q.contains("stock") || q.contains("warehouse")) {
			var stock = inventoryService.listStock();
			long low = stock.stream().filter(s -> s.availableQty() <= 2).count();
			Map<String, Object> inv = Map.of(
					"stockItems", stock.size(),
					"lowStockItems", low
			);
			metrics.putAll(inv);
			tools.add(new ToolTrace("getInventoryAvailability", Map.of(), inv));
		}

		if (q.contains("policy") || q.contains("procedure") || q.contains("runbook") || q.contains("damaged")) {
			var grounded = knowledgeService.query(question, 3);
			tools.add(new ToolTrace("searchKnowledgeBase", Map.of("question", question), Map.of(
					"citations", grounded.citations().size(),
					"insufficientEvidence", grounded.insufficientEvidence()
			)));
			return new CopilotResponse(
					grounded.answer(),
					grounded.confidence(),
					metrics,
					tools,
					grounded.citations().stream()
							.map(c -> Map.<String, Object>of("type", "document", "title", c.documentTitle(), "ref", c.ref()))
							.toList(),
					List.of("Open Knowledge Base for full source text.")
			);
		}

		String summary = metrics.isEmpty()
				? "I can answer questions about orders, inventory, or indexed knowledge documents. Try asking about cancelled orders or stock availability."
				: "Operational snapshot based on live application tools.";

		return new CopilotResponse(
				summary,
				metrics.isEmpty() ? "low" : "medium",
				metrics,
				tools,
				tools.stream().map(t -> Map.<String, Object>of("type", "metric", "source", t.name())).toList(),
				List.of("Ask: Why did cancelled orders increase?", "Ask: Which products have low stock?")
		);
	}

	public record ToolTrace(String name, Map<String, Object> arguments, Map<String, Object> result) {
	}

	public record CopilotResponse(
			String summary,
			String confidence,
			Map<String, Object> metrics,
			List<ToolTrace> tools,
			List<Map<String, Object>> references,
			List<String> recommendations
	) {
	}
}
