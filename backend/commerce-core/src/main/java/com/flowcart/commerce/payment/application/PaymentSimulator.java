package com.flowcart.commerce.payment.application;

import com.flowcart.commerce.chaos.ChaosService;
import com.flowcart.commerce.payment.domain.Payment;
import com.flowcart.commerce.payment.domain.PaymentStatus;
import com.flowcart.commerce.payment.domain.SimulatedPaymentOutcome;
import com.flowcart.commerce.shared.domain.Money;
import com.flowcart.commerce.shared.observability.CommerceMetrics;
import java.util.UUID;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class PaymentSimulator {

	private final ObjectProvider<ChaosService> chaosService;
	private final ObjectProvider<CommerceMetrics> commerceMetrics;

	public PaymentSimulator(
			ObjectProvider<ChaosService> chaosService,
			ObjectProvider<CommerceMetrics> commerceMetrics
	) {
		this.chaosService = chaosService;
		this.commerceMetrics = commerceMetrics;
	}

	public Payment charge(UUID orderId, String idempotencyKey, Money amount, SimulatedPaymentOutcome outcome) {
		ChaosService chaos = chaosService.getIfAvailable();
		if (chaos != null && chaos.shouldDeclinePayments()) {
			outcome = SimulatedPaymentOutcome.DECLINED;
		}
		Payment payment = switch (outcome) {
			case SUCCESS -> Payment.create(
					orderId,
					idempotencyKey,
					amount,
					PaymentStatus.AUTHORIZED,
					"sim-" + UUID.randomUUID(),
					null
			);
			case DECLINED -> Payment.create(
					orderId,
					idempotencyKey,
					amount,
					PaymentStatus.DECLINED,
					null,
					"Card declined by simulated provider"
			);
			case TIMEOUT -> Payment.create(
					orderId,
					idempotencyKey,
					amount,
					PaymentStatus.TIMEOUT,
					null,
					"Simulated provider timeout"
			);
		};
		if (payment.status() != PaymentStatus.AUTHORIZED) {
			CommerceMetrics metrics = commerceMetrics.getIfAvailable();
			if (metrics != null) {
				metrics.paymentFailed();
			}
		}
		return payment;
	}
}
