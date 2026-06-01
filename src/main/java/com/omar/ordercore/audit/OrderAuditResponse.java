package com.omar.ordercore.audit;

import com.omar.ordercore.domain.valueobject.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderAuditResponse {
    private String id;
    private OrderStatus fromStatus;
    private OrderStatus toStatus;
    private String triggeredBy;
    private String note;
    private LocalDateTime occurredAt;
}
