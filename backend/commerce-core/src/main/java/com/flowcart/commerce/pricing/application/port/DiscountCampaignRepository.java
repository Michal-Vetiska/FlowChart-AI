package com.flowcart.commerce.pricing.application.port;

import com.flowcart.commerce.pricing.domain.DiscountCampaign;
import java.util.Optional;

public interface DiscountCampaignRepository {

	DiscountCampaign save(DiscountCampaign campaign);

	Optional<DiscountCampaign> findByCode(String code);
}
