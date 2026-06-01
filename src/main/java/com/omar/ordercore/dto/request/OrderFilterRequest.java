package com.omar.ordercore.dto.request;

import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderFilterRequest {
    private String storeId;
    private String customerId;
    private OrderStatus status;
    private OrderClassification classification;
    private Double minPrice;
    private Double maxPrice;
}
