package com.flowcart.commerce.catalog.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.Money;
import org.junit.jupiter.api.Test;

class ProductTest {

	@Test
	void createsNormalizedSkuAndActiveProduct() {
		Product product = Product.create("sku-001", "  Widget  ", "desc", Money.of("99.90", "CZK"), null);
		assertThat(product.sku()).isEqualTo("SKU-001");
		assertThat(product.name()).isEqualTo("Widget");
		assertThat(product.active()).isTrue();
	}

	@Test
	void rejectsZeroPrice() {
		assertThatThrownBy(() -> Product.create("SKU-1", "X", null, Money.zero("CZK"), null))
				.isInstanceOf(DomainException.class);
	}
}
