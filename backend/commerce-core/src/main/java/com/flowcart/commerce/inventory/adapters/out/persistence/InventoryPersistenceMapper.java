package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.domain.StockItem;
import com.flowcart.commerce.inventory.domain.StockMovement;
import com.flowcart.commerce.inventory.domain.StockReservation;
import com.flowcart.commerce.inventory.domain.Warehouse;

final class InventoryPersistenceMapper {

	private InventoryPersistenceMapper() {
	}

	static WarehouseJpaEntity toEntity(Warehouse warehouse) {
		WarehouseJpaEntity entity = new WarehouseJpaEntity();
		entity.setId(warehouse.id());
		entity.setCode(warehouse.code());
		entity.setName(warehouse.name());
		entity.setLocation(warehouse.location());
		entity.setActive(warehouse.active());
		entity.setCreatedAt(warehouse.createdAt());
		entity.setUpdatedAt(warehouse.updatedAt());
		return entity;
	}

	static Warehouse toDomain(WarehouseJpaEntity entity) {
		return Warehouse.rehydrate(
				entity.getId(),
				entity.getCode(),
				entity.getName(),
				entity.getLocation(),
				entity.isActive(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static StockItemJpaEntity toEntity(StockItem stockItem) {
		StockItemJpaEntity entity = new StockItemJpaEntity();
		entity.setId(stockItem.id());
		entity.setWarehouseId(stockItem.warehouseId());
		entity.setProductId(stockItem.productId());
		entity.setAvailableQty(stockItem.availableQty());
		entity.setReservedQty(stockItem.reservedQty());
		entity.setVersion(stockItem.version());
		entity.setCreatedAt(stockItem.createdAt());
		entity.setUpdatedAt(stockItem.updatedAt());
		return entity;
	}

	static StockItem toDomain(StockItemJpaEntity entity) {
		return StockItem.rehydrate(
				entity.getId(),
				entity.getWarehouseId(),
				entity.getProductId(),
				entity.getAvailableQty(),
				entity.getReservedQty(),
				entity.getVersion(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static StockReservationJpaEntity toEntity(StockReservation reservation) {
		StockReservationJpaEntity entity = new StockReservationJpaEntity();
		entity.setId(reservation.id());
		entity.setStockItemId(reservation.stockItemId());
		entity.setOrderId(reservation.orderId());
		entity.setQuantity(reservation.quantity());
		entity.setStatus(reservation.status());
		entity.setExpiresAt(reservation.expiresAt());
		entity.setCreatedAt(reservation.createdAt());
		entity.setUpdatedAt(reservation.updatedAt());
		return entity;
	}

	static StockReservation toDomain(StockReservationJpaEntity entity) {
		return StockReservation.rehydrate(
				entity.getId(),
				entity.getStockItemId(),
				entity.getOrderId(),
				entity.getQuantity(),
				entity.getStatus(),
				entity.getExpiresAt(),
				entity.getCreatedAt(),
				entity.getUpdatedAt()
		);
	}

	static StockMovementJpaEntity toEntity(StockMovement movement) {
		StockMovementJpaEntity entity = new StockMovementJpaEntity();
		entity.setId(movement.id());
		entity.setStockItemId(movement.stockItemId());
		entity.setMovementType(movement.movementType());
		entity.setQuantity(movement.quantity());
		entity.setReason(movement.reason());
		entity.setReferenceId(movement.referenceId());
		entity.setCreatedAt(movement.createdAt());
		return entity;
	}

	static StockMovement toDomain(StockMovementJpaEntity entity) {
		return StockMovement.rehydrate(
				entity.getId(),
				entity.getStockItemId(),
				entity.getMovementType(),
				entity.getQuantity(),
				entity.getReason(),
				entity.getReferenceId(),
				entity.getCreatedAt()
		);
	}
}
