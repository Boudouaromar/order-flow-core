package com.omar.ordercore.audit;

import com.omar.ordercore.domain.valueobject.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_audit_history", indexes = {
        @Index(name = "idx_audit_order_id", columnList = "order_id"),
        @Index(name = "idx_audit_occurred_at", columnList = "occurred_at")
})
@EntityListeners(AuditingEntityListener.class)
public class OrderAuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "order_id", nullable = false)
    private String orderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status")
    private OrderStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", nullable = false)
    private OrderStatus toStatus;

    @Column(name = "triggered_by")
    private String triggeredBy;

    @Column(name = "note")
    private String note;

    @CreatedDate
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
