package com.omar.ordercore.mapper;

import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.dto.response.OrderResponse;
import com.omar.ordercore.dto.response.OrderSummaryResponse;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .storeId(order.getStoreId())
                .storeCountry(order.getStoreCountry())
                .customerId(order.getCustomerId())
                .product(order.getProduct())
                .category(order.getCategory())
                .quantity(order.getQuantity())
                .unitPrice(order.getUnitPrice())
                .totalPrice(order.getTotalPrice())
                .discountApplied(order.getDiscountApplied())
                .finalPrice(order.getFinalPrice())
                .classification(order.getClassification())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .processedAt(order.getProcessedAt())
                .version(order.getVersion())
                .build();
    }

    public OrderSummaryResponse toSummary(Order order) {
        return OrderSummaryResponse.builder()
                .id(order.getId())
                .storeId(order.getStoreId())
                .customerId(order.getCustomerId())
                .product(order.getProduct())
                .finalPrice(order.getFinalPrice())
                .classification(order.getClassification() != null ? order.getClassification().name() : null)
                .status(order.getStatus() != null ? order.getStatus().name() : null)
                .build();
    }
}
