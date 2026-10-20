package com.flowcart.commerce.chaos;

import com.flowcart.commerce.chaos.ChaosService.ChaosRun;
import com.flowcart.commerce.chaos.ChaosService.ChaosStatus;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chaos")
public class ChaosController {

	private final ChaosService chaosService;

	public ChaosController(ChaosService chaosService) {
		this.chaosService = chaosService;
	}

	public record StartRequest(String scenarioType, Map<String, Object> parameters) {
	}

	@GetMapping("/status")
	public ChaosStatus status() {
		return chaosService.status();
	}

	@PostMapping("/start")
	public ChaosRun start(@RequestBody StartRequest request) {
		return chaosService.start(
				request.scenarioType(),
				request.parameters() == null ? Map.of() : request.parameters()
		);
	}

	@PostMapping("/reset")
	public ChaosStatus reset() {
		chaosService.reset();
		return chaosService.status();
	}
}
