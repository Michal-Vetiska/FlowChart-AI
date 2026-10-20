package com.flowcart.commerce.messaging.adapters.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataProcessedEventJpaRepository extends JpaRepository<ProcessedEventJpaEntity, UUID> {
}
