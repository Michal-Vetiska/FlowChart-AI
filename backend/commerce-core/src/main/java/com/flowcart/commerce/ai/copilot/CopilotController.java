package com.flowcart.commerce.ai.copilot;

import com.flowcart.commerce.ai.copilot.CopilotService.CopilotResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/copilot")
public class CopilotController {

	private final CopilotService copilotService;

	public CopilotController(CopilotService copilotService) {
		this.copilotService = copilotService;
	}

	public record AskRequest(@NotBlank String question) {
	}

	@PostMapping("/ask")
	public CopilotResponse ask(@RequestBody AskRequest request) {
		return copilotService.ask(request.question());
	}
}
