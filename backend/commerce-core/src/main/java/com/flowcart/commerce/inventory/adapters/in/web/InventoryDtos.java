package com.flowcart.commerce.inventory.adapters.in.web;

import com.flowcart.commerce.inventory.domain.StockItem;
import com.flowcart.commerce.inventory.domain.StockMovement;
import com.flowcart.commerce.inventory.domain.Warehouse;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class InventoryDtos {

	private InventoryDtos() {
	}

	public record CreateWarehouseRequest(
			@NotBlank @Size(max = 32) String code,
			@NotBlank @Size(max = 120) String name,
			@Size(max = 255) String location
	) {
	}

	public record WarehouseResponse(
			UUID id,
			String code,
			String name,
			String location,
			boolean active,
			Instant createdAt,
			Instant updatedAt
	) {
		public static WarehouseResponse from(Warehouse warehouse) {
			return new WarehouseResponse(
					warehouse.id(),
					warehouse.code(),
					warehouse.name(),
					warehouse.location(),
					warehouse.active(),
					warehouse.createdAt(),
					warehouse.updatedAt()
			);
		}
	}

	public record UpsertStockRequest(
			@NotNull UUID warehouseId,
			@NotNull UUID productId,
			@Min(0) int availableQty,
			String reason
	) {
	}

	public record StockItemResponse(
			UUID id,
			UUID warehouseId,
			UUID productId,
			int availableQty,
			int reservedQty,
			long version,
			Instant updatedAt
	) {
		public static StockItemResponse from(StockItem item) {
			return new StockItemResponse(
					item.id(),
					item.warehouseId(),
					item.productId(),
					item.availableQty(),
					item.reservedQty(),
					item.version(),
					item.updatedAt()
			);
		}
	}

	public record StockMovementResponse(
			UUID id,
			UUID stockItemId,
			String movementType,
			int quantity,
			String reason,
			UUID referenceId,
			Instant createdAt
	) {
		public static StockMovementResponse from(StockMovement movement) {
			return new StockMovementResponse(
					movement.id(),
					movement.stockItemId(),
					movement.movementType().name(),
					movement.quantity(),
					movement.reason(),
					movement.referenceId(),
					movement.createdAt()
			);
		}
	}
}
