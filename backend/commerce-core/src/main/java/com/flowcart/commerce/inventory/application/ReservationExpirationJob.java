package com.flowcart.commerce.inventory.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationJob {

	private static final Logger log = LoggerFactory.getLogger(ReservationExpirationJob.class);

	private final InventoryService inventoryService;

	public ReservationExpirationJob(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@Scheduled(fixedDelayString = "${flowcart.inventory.expiration-poll-ms:15000}")
	public void expireReservations() {
		int expired = inventoryService.expireDueReservations();
		if (expired > 0) {
			log.info("Expired {} stock reservation(s)", expired);
		}
	}
}
