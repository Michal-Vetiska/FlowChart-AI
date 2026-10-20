package com.flowcart.commerce.ai.copilot;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.flowcart.commerce.ai.knowledge.KnowledgeService;
import com.flowcart.commerce.ai.knowledge.KnowledgeService.Citation;
import com.flowcart.commerce.ai.knowledge.KnowledgeService.GroundedAnswer;
import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.ordering.application.port.OrderRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CopilotServiceTest {

	@Test
	void usesKnowledgeToolForProcedureQuestions() {
		OrderRepository orders = mock(OrderRepository.class);
		InventoryService inventory = mock(InventoryService.class);
		KnowledgeService knowledge = mock(KnowledgeService.class);
		when(knowledge.query(anyString(), anyInt())).thenReturn(new GroundedAnswer(
				"Quarantine damaged SKU",
				"medium",
				List.of(new Citation(UUID.randomUUID(), "Damaged Goods", 1, 1)),
				false
		));
		when(orders.findAll()).thenReturn(List.of());

		CopilotService service = new CopilotService(orders, inventory, knowledge);
		var response = service.ask("What is the damaged goods procedure?");

		assertThat(response.summary()).contains("Quarantine");
		assertThat(response.tools()).extracting(CopilotService.ToolTrace::name)
				.contains("searchKnowledgeBase");
	}

	@Test
	void answersWithGuidanceWhenNoSignal() {
		OrderRepository orders = mock(OrderRepository.class);
		when(orders.findAll()).thenReturn(List.of());
		CopilotService service = new CopilotService(
				orders,
				mock(InventoryService.class),
				mock(KnowledgeService.class)
		);
		var response = service.ask("hello");
		assertThat(response.confidence()).isEqualTo("low");
		assertThat(response.tools()).isEmpty();
		assertThat(response.recommendations()).isNotEmpty();
	}
}
