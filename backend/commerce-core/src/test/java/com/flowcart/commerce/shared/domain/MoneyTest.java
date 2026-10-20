package com.flowcart.commerce.shared.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class MoneyTest {

	@Test
	void addsSameCurrency() {
		Money a = Money.of("10.00", "CZK");
		Money b = Money.of("2.50", "CZK");
		assertThat(a.add(b)).isEqualTo(Money.of("12.50", "CZK"));
	}

	@Test
	void rejectsCurrencyMismatch() {
		assertThatThrownBy(() -> Money.of("10", "CZK").add(Money.of("1", "EUR")))
				.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void percentageDiscount() {
		Money price = Money.of(new BigDecimal("200.0000"), "CZK");
		assertThat(price.percentageOf(new BigDecimal("10"))).isEqualTo(Money.of("20.0000", "CZK"));
	}
}
