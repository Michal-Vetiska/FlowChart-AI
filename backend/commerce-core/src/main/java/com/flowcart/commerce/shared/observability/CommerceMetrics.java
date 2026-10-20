package com.flowcart.commerce.shared.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

@Component
public class CommerceMetrics {

	private final Counter reservationFailures;
	private final Counter paymentFailures;
	private final Timer orderLatency;

	public CommerceMetrics(MeterRegistry registry) {
		this.reservationFailures = Counter.builder("flowcart.reservation.failures")
				.description("Stock reservation failures")
				.register(registry);
		this.paymentFailures = Counter.builder("flowcart.payment.failures")
				.description("Payment declines / timeouts")
				.register(registry);
		this.orderLatency = Timer.builder("flowcart.order.latency")
				.description("Order create-to-confirm latency")
				.register(registry);
	}

	public void reservationFailed() {
		reservationFailures.increment();
	}

	public void paymentFailed() {
		paymentFailures.increment();
	}

	public Timer.Sample startOrderTimer() {
		return Timer.start();
	}

	public void recordOrderLatency(Timer.Sample sample) {
		sample.stop(orderLatency);
	}
}
