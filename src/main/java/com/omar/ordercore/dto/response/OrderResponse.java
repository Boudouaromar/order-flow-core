package com.omar.ordercore.dto.response;

import com.omar.ordercore.domain.valueobject.OrderCategory;
import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private String id;
    private String storeId;
    private String storeCountry;
    private String customerId;
    private String product;
    private OrderCategory category;
    private Integer quantity;
    private Double unitPrice;
    private Double totalPrice;
    private Double discountApplied;
    private Double finalPrice;
    private OrderClassification classification;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private Long version;
}
