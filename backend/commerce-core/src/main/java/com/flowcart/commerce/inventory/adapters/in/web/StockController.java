package com.flowcart.commerce.inventory.adapters.in.web;

import com.flowcart.commerce.inventory.adapters.in.web.InventoryDtos.StockItemResponse;
import com.flowcart.commerce.inventory.adapters.in.web.InventoryDtos.StockMovementResponse;
import com.flowcart.commerce.inventory.adapters.in.web.InventoryDtos.UpsertStockRequest;
import com.flowcart.commerce.inventory.application.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/stock")
public class StockController {

	private final InventoryService inventoryService;

	public StockController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PutMapping
	@ResponseStatus(HttpStatus.OK)
	public StockItemResponse upsert(@Valid @RequestBody UpsertStockRequest request) {
		return StockItemResponse.from(inventoryService.createOrAdjustStock(
				request.warehouseId(),
				request.productId(),
				request.availableQty(),
				request.reason()
		));
	}

	@GetMapping
	public List<StockItemResponse> list() {
		return inventoryService.listStock().stream().map(StockItemResponse::from).toList();
	}

	@GetMapping("/{stockItemId}/movements")
	public List<StockMovementResponse> movements(@PathVariable UUID stockItemId) {
		return inventoryService.movementsForStockItem(stockItemId).stream()
				.map(StockMovementResponse::from)
				.toList();
	}
}
