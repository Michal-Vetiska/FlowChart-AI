package com.flowcart.commerce.inventory.application.port;

import com.flowcart.commerce.inventory.domain.StockReservation;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockReservationRepository {

	StockReservation save(StockReservation reservation);

	Optional<StockReservation> findById(UUID id);

	List<StockReservation> findActiveByOrderId(UUID orderId);

	List<StockReservation> findExpiredActive(java.time.Instant now);
}
