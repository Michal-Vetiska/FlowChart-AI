package com.flowcart.commerce.inventory.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flowcart.commerce.shared.domain.DomainException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StockItemTest {

	@Test
	void reserveMovesAvailabilityToReserved() {
		StockItem item = StockItem.create(UUID.randomUUID(), UUID.randomUUID(), 10);
		item.reserve(3);
		assertThat(item.availableQty()).isEqualTo(7);
		assertThat(item.reservedQty()).isEqualTo(3);
	}

	@Test
	void cannotOversell() {
		StockItem item = StockItem.create(UUID.randomUUID(), UUID.randomUUID(), 2);
		assertThatThrownBy(() -> item.reserve(3)).isInstanceOf(DomainException.class);
	}

	@Test
	void releaseReturnsStock() {
		StockItem item = StockItem.create(UUID.randomUUID(), UUID.randomUUID(), 5);
		item.reserve(2);
		item.release(2);
		assertThat(item.availableQty()).isEqualTo(5);
		assertThat(item.reservedQty()).isEqualTo(0);
	}
}
