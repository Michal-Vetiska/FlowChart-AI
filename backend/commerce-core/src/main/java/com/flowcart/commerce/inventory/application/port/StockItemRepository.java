package com.flowcart.commerce.inventory.application.port;

import com.flowcart.commerce.inventory.domain.StockItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockItemRepository {

	StockItem save(StockItem stockItem);

	Optional<StockItem> findById(UUID id);

	Optional<StockItem> findByWarehouseAndProduct(UUID warehouseId, UUID productId);

	Optional<StockItem> findByIdForUpdate(UUID id);

	List<StockItem> findAll();
}
