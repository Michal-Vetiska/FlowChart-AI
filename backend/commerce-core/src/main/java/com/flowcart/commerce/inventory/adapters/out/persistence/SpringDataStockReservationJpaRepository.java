package com.flowcart.commerce.inventory.adapters.out.persistence;

import com.flowcart.commerce.inventory.domain.ReservationStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataStockReservationJpaRepository extends JpaRepository<StockReservationJpaEntity, UUID> {

	List<StockReservationJpaEntity> findByOrderIdAndStatus(UUID orderId, ReservationStatus status);

	@Query("""
			select r from StockReservationJpaEntity r
			where r.status = :status and r.expiresAt is not null and r.expiresAt <= :now
			""")
	List<StockReservationJpaEntity> findExpiredActive(
			@Param("status") ReservationStatus status,
			@Param("now") Instant now
	);
}
