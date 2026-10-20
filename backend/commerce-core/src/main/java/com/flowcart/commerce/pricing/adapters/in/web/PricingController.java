package com.flowcart.commerce.pricing.adapters.in.web;

import com.flowcart.commerce.pricing.application.PricingService;
import com.flowcart.commerce.pricing.domain.DiscountCampaign;
import com.flowcart.commerce.pricing.domain.DiscountType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pricing/campaigns")
public class PricingController {

	private final PricingService pricingService;

	public PricingController(PricingService pricingService) {
		this.pricingService = pricingService;
	}

	public record CreateCampaignRequest(
			@NotBlank @Size(max = 64) String code,
			@NotBlank @Size(max = 120) String name,
			@NotNull DiscountType discountType,
			@NotNull @DecimalMin("0.01") BigDecimal discountValue,
			@NotNull Instant validFrom,
			@NotNull Instant validTo
	) {
	}

	public record CampaignResponse(
			UUID id,
			String code,
			String name,
			String discountType,
			BigDecimal discountValue,
			Instant validFrom,
			Instant validTo,
			boolean active
	) {
		static CampaignResponse from(DiscountCampaign campaign) {
			return new CampaignResponse(
					campaign.id(),
					campaign.code(),
					campaign.name(),
					campaign.discountType().name(),
					campaign.discountValue(),
					campaign.validFrom(),
					campaign.validTo(),
					campaign.active()
			);
		}
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CampaignResponse create(@Valid @RequestBody CreateCampaignRequest request) {
		return CampaignResponse.from(pricingService.createCampaign(
				request.code(),
				request.name(),
				request.discountType(),
				request.discountValue(),
				request.validFrom(),
				request.validTo()
		));
	}
}
