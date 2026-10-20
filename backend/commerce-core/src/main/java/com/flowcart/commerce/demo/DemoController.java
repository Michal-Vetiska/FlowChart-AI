package com.flowcart.commerce.demo;

import com.flowcart.commerce.demo.ConcurrencyBurstService.ConcurrencyBurstResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/demo")
public class DemoController {

	private final ConcurrencyBurstService concurrencyBurstService;
	private final boolean demoEnabled;

	public DemoController(
			ConcurrencyBurstService concurrencyBurstService,
			@Value("${flowcart.demo.enabled:true}") boolean demoEnabled
	) {
		this.concurrencyBurstService = concurrencyBurstService;
		this.demoEnabled = demoEnabled;
	}

	public record ConcurrencyBurstRequest(
			@NotNull UUID warehouseId,
			@NotNull UUID productId,
			@Min(1) @Max(1000) int stockQty,
			@Min(1) @Max(5000) int concurrentRequests
	) {
	}

	@PostMapping("/concurrency-burst")
	public ConcurrencyBurstResult concurrencyBurst(@Valid @RequestBody ConcurrencyBurstRequest request) {
		assertDemoEnabled();
		return concurrencyBurstService.runBurst(
				request.warehouseId(),
				request.productId(),
				request.stockQty(),
				request.concurrentRequests()
		);
	}

	private void assertDemoEnabled() {
		if (!demoEnabled) {
			throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Demo endpoints are disabled");
		}
	}
}
