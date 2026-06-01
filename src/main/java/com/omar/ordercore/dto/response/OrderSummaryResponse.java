package com.omar.ordercore.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {
    private String id;
    private String storeId;
    private String customerId;
    private String product;
    private Double finalPrice;
    private String classification;
    private String status;
}
