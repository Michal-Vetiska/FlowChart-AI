package com.flowcart.commerce.shared.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Objects;

public final class Money {

	private static final int SCALE = 4;

	private final BigDecimal amount;
	private final Currency currency;

	private Money(BigDecimal amount, Currency currency) {
		Objects.requireNonNull(amount, "amount");
		Objects.requireNonNull(currency, "currency");
		if (amount.scale() > SCALE) {
			throw new IllegalArgumentException("Money scale must be <= " + SCALE);
		}
		this.amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
		this.currency = currency;
	}

	public static Money of(BigDecimal amount, String currencyCode) {
		return new Money(amount, Currency.getInstance(currencyCode));
	}

	public static Money of(String amount, String currencyCode) {
		return of(new BigDecimal(amount), currencyCode);
	}

	public static Money zero(String currencyCode) {
		return of(BigDecimal.ZERO, currencyCode);
	}

	public BigDecimal amount() {
		return amount;
	}

	public Currency currency() {
		return currency;
	}

	public String currencyCode() {
		return currency.getCurrencyCode();
	}

	public Money add(Money other) {
		assertSameCurrency(other);
		return new Money(amount.add(other.amount), currency);
	}

	public Money subtract(Money other) {
		assertSameCurrency(other);
		return new Money(amount.subtract(other.amount), currency);
	}

	public Money multiply(int quantity) {
		if (quantity < 0) {
			throw new IllegalArgumentException("quantity must be >= 0");
		}
		return new Money(amount.multiply(BigDecimal.valueOf(quantity)), currency);
	}

	public Money percentageOf(BigDecimal percent) {
		BigDecimal factor = percent.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
		BigDecimal result = amount.multiply(factor).setScale(SCALE, RoundingMode.HALF_UP);
		return new Money(result, currency);
	}

	public boolean isNegative() {
		return amount.signum() < 0;
	}

	public boolean isZero() {
		return amount.signum() == 0;
	}

	private void assertSameCurrency(Money other) {
		if (!currency.equals(other.currency)) {
			throw new IllegalArgumentException("Currency mismatch: " + currencyCode() + " vs " + other.currencyCode());
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Money money)) {
			return false;
		}
		return amount.compareTo(money.amount) == 0 && currency.equals(money.currency);
	}

	@Override
	public int hashCode() {
		return Objects.hash(amount.stripTrailingZeros(), currency);
	}

	@Override
	public String toString() {
		return amount.toPlainString() + " " + currencyCode();
	}
}
