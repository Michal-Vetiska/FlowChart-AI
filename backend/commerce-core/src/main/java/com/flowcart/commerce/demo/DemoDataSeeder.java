package com.flowcart.commerce.demo;

import com.flowcart.commerce.ai.knowledge.KnowledgeService;
import com.flowcart.commerce.catalog.application.CreateProductCommand;
import com.flowcart.commerce.catalog.application.ProductService;
import com.flowcart.commerce.catalog.domain.Product;
import com.flowcart.commerce.inventory.application.InventoryService;
import com.flowcart.commerce.inventory.domain.Warehouse;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoDataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

	private final boolean enabled;
	private final ProductService productService;
	private final InventoryService inventoryService;
	private final KnowledgeService knowledgeService;

	public DemoDataSeeder(
			@Value("${flowcart.demo.seed-on-startup:false}") boolean enabled,
			ProductService productService,
			InventoryService inventoryService,
			KnowledgeService knowledgeService
	) {
		this.enabled = enabled;
		this.productService = productService;
		this.inventoryService = inventoryService;
		this.knowledgeService = knowledgeService;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!enabled) {
			return;
		}
		if (!productService.list().isEmpty()) {
			log.info("Demo seed skipped — catalog already has products");
			return;
		}
		log.info("Seeding demo commerce + knowledge data");
		Product widget = productService.create(new CreateProductCommand(
				"FC-WIDGET-1",
				"Flow Widget",
				"Interview demo SKU",
				new BigDecimal("149.90"),
				"CZK",
				null
		));
		Product cable = productService.create(new CreateProductCommand(
				"FC-CABLE-1",
				"Flow Cable",
				"Low-stock demo SKU",
				new BigDecimal("39.90"),
				"CZK",
				null
		));
		Warehouse wh = inventoryService.createWarehouse("PRG-1", "Prague DC", "CZ");
		inventoryService.createOrAdjustStock(wh.id(), widget.id(), 10, "demo seed");
		inventoryService.createOrAdjustStock(wh.id(), cable.id(), 2, "demo seed low stock");

		knowledgeService.ingestText(
				"Damaged Goods Runbook",
				"damaged-goods.md",
				"text/markdown",
				"""
						# Damaged Goods Procedure
						1. Quarantine the SKU in the warehouse location.
						2. Create a stock adjustment with reason DAMAGED.
						3. Notify customer support if an active order is affected.
						4. Escalate to Chaos Lab / Incident Investigator if payment failures spike simultaneously.
						"""
		);
		knowledgeService.ingestText(
				"Payment Decline Incident Pattern",
				"payment-decline-incident.md",
				"text/markdown",
				"""
						# Payment Decline Incident Pattern
						Symptoms: sudden rise in CANCELLED / PAYMENT_FAILED orders.
						Evidence: Chaos Lab paymentForceDecline flag or simulated provider declines.
						Remediation: reset chaos flags, replay a successful payment, inspect Tempo spans for payment authorize.
						"""
		);
		log.info("Demo seed complete (warehouse={}, products=2, docs=2)", wh.code());
	}
}
