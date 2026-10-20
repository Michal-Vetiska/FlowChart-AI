package com.flowcart.commerce.pricing.adapters.out.persistence;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataDiscountCampaignJpaRepository extends JpaRepository<DiscountCampaignJpaEntity, UUID> {

	Optional<DiscountCampaignJpaEntity> findByCode(String code);
}
