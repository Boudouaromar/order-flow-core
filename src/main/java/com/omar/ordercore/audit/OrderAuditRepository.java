package com.omar.ordercore.audit;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderAuditRepository extends JpaRepository<OrderAuditEvent, String> {
    List<OrderAuditEvent> findByOrderIdOrderByOccurredAtAsc(String orderId);
}
