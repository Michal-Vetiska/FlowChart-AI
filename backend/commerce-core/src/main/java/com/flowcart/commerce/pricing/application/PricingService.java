package com.flowcart.commerce.pricing.application;

import com.flowcart.commerce.pricing.application.port.DiscountCampaignRepository;
import com.flowcart.commerce.pricing.domain.DiscountCampaign;
import com.flowcart.commerce.pricing.domain.DiscountType;
import com.flowcart.commerce.shared.domain.DomainException;
import com.flowcart.commerce.shared.domain.Money;
import com.flowcart.commerce.shared.domain.ResourceNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PricingService {

	private final DiscountCampaignRepository discountCampaignRepository;

	public PricingService(DiscountCampaignRepository discountCampaignRepository) {
		this.discountCampaignRepository = discountCampaignRepository;
	}

	@Transactional
	public DiscountCampaign createCampaign(
			String code,
			String name,
			DiscountType type,
			BigDecimal value,
			Instant validFrom,
			Instant validTo
	) {
		if (discountCampaignRepository.findByCode(code).isPresent()) {
			throw new DomainException("Coupon already exists: " + code);
		}
		return discountCampaignRepository.save(DiscountCampaign.create(code, name, type, value, validFrom, validTo));
	}

	@Transactional(readOnly = true)
	public Money calculateDiscount(Money subtotal, String couponCode) {
		if (couponCode == null || couponCode.isBlank()) {
			return Money.zero(subtotal.currencyCode());
		}
		DiscountCampaign campaign = discountCampaignRepository.findByCode(couponCode)
				.orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponCode));
		if (!campaign.isValidAt(Instant.now())) {
			throw new DomainException("Coupon is not valid at this time: " + couponCode);
		}
		return switch (campaign.discountType()) {
			case PERCENTAGE -> subtotal.percentageOf(campaign.discountValue());
			case FIXED -> {
				Money fixed = Money.of(campaign.discountValue(), subtotal.currencyCode());
				yield fixed.amount().compareTo(subtotal.amount()) > 0 ? subtotal : fixed;
			}
		};
	}
}
