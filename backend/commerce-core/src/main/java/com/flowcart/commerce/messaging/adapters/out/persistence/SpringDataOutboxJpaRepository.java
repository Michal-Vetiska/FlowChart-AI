package com.flowcart.commerce.messaging.adapters.out.persistence;

import com.flowcart.commerce.messaging.domain.OutboxStatus;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataOutboxJpaRepository extends JpaRepository<OutboxMessageJpaEntity, UUID> {

	List<OutboxMessageJpaEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
