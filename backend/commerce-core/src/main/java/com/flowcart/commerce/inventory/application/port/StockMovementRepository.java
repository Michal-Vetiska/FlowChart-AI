package com.flowcart.commerce.inventory.application.port;

import com.flowcart.commerce.inventory.domain.StockMovement;
import java.util.List;
import java.util.UUID;

public interface StockMovementRepository {

	StockMovement save(StockMovement movement);

	List<StockMovement> findByStockItemId(UUID stockItemId);
}
