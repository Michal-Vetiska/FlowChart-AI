package com.flowcart.commerce.pricing.adapters.out.persistence;

import com.flowcart.commerce.pricing.application.port.DiscountCampaignRepository;
import com.flowcart.commerce.pricing.domain.DiscountCampaign;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DiscountCampaignRepositoryAdapter implements DiscountCampaignRepository {

	private final SpringDataDiscountCampaignJpaRepository jpaRepository;

	public DiscountCampaignRepositoryAdapter(SpringDataDiscountCampaignJpaRepository jpaRepository) {
		this.jpaRepository = jpaRepository;
	}

	@Override
	public DiscountCampaign save(DiscountCampaign campaign) {
		DiscountCampaignJpaEntity entity = new DiscountCampaignJpaEntity();
		entity.setId(campaign.id());
		entity.setCode(campaign.code());
		entity.setName(campaign.name());
		entity.setDiscountType(campaign.discountType());
		entity.setDiscountValue(campaign.discountValue());
		entity.setValidFrom(campaign.validFrom());
		entity.setValidTo(campaign.validTo());
		entity.setActive(campaign.active());
		entity.setCreatedAt(campaign.createdAt());
		entity.setUpdatedAt(campaign.updatedAt());
		DiscountCampaignJpaEntity saved = jpaRepository.save(entity);
		return DiscountCampaign.rehydrate(
				saved.getId(),
				saved.getCode(),
				saved.getName(),
				saved.getDiscountType(),
				saved.getDiscountValue(),
				saved.getValidFrom(),
				saved.getValidTo(),
				saved.isActive(),
				saved.getCreatedAt(),
				saved.getUpdatedAt()
		);
	}

	@Override
	public Optional<DiscountCampaign> findByCode(String code) {
		return jpaRepository.findByCode(code.trim().toUpperCase()).map(saved -> DiscountCampaign.rehydrate(
				saved.getId(),
				saved.getCode(),
				saved.getName(),
				saved.getDiscountType(),
				saved.getDiscountValue(),
				saved.getValidFrom(),
				saved.getValidTo(),
				saved.isActive(),
				saved.getCreatedAt(),
				saved.getUpdatedAt()
		));
	}
}
