package com.flowcart.commerce.inventory.adapters.in.web;

import com.flowcart.commerce.inventory.adapters.in.web.InventoryDtos.CreateWarehouseRequest;
import com.flowcart.commerce.inventory.adapters.in.web.InventoryDtos.WarehouseResponse;
import com.flowcart.commerce.inventory.application.InventoryService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/warehouses")
public class WarehouseController {

	private final InventoryService inventoryService;

	public WarehouseController(InventoryService inventoryService) {
		this.inventoryService = inventoryService;
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public WarehouseResponse create(@Valid @RequestBody CreateWarehouseRequest request) {
		return WarehouseResponse.from(inventoryService.createWarehouse(request.code(), request.name(), request.location()));
	}

	@GetMapping
	public List<WarehouseResponse> list() {
		return inventoryService.listWarehouses().stream().map(WarehouseResponse::from).toList();
	}

	@GetMapping("/{id}")
	public WarehouseResponse get(@PathVariable UUID id) {
		return WarehouseResponse.from(inventoryService.getWarehouse(id));
	}
}
