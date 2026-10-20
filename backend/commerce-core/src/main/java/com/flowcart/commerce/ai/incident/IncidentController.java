package com.flowcart.commerce.ai.incident;

import com.flowcart.commerce.ai.incident.IncidentInvestigatorService.IncidentReport;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai/incidents")
public class IncidentController {

	private final IncidentInvestigatorService investigatorService;

	public IncidentController(IncidentInvestigatorService investigatorService) {
		this.investigatorService = investigatorService;
	}

	public record InvestigateRequest(String symptom) {
	}

	@PostMapping("/investigate")
	public IncidentReport investigate(@RequestBody InvestigateRequest request) {
		return investigatorService.investigate(request.symptom());
	}
}
