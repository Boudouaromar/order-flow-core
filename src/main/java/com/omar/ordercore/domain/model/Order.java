package com.omar.ordercore.domain.model;

import com.omar.ordercore.domain.valueobject.OrderCategory;
import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_store_id", columnList = "store_id"),
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_classification", columnList = "classification")
})
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "store_id", nullable = false)
    private String storeId;

    @Column(name = "store_country")
    private String storeCountry;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderCategory category;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "discount_applied")
    private Double discountApplied;

    @Column(name = "final_price")
    private Double finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderClassification classification;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    // Optimistic locking — prevents concurrent updates from overwriting each other
    @Version
    private Long version;
}
