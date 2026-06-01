package com.omar.ordercore.repository;

import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.domain.valueobject.OrderCategory;
import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository slice tests using @DataJpaTest.
 * Loads only the JPA layer — fast and focused.
 */
@ActiveProfiles("test")
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();

        orderRepository.save(order("STORE-LIS", "CUST-101", OrderClassification.STANDARD, OrderStatus.PROCESSED, 59.97));
        orderRepository.save(order("STORE-LIS", "CUST-202", OrderClassification.LARGE, OrderStatus.PROCESSED, 650.0));
        orderRepository.save(order("STORE-OPO", "CUST-303", OrderClassification.VIP, OrderStatus.PROCESSED, 1200.0));
        orderRepository.save(order("STORE-MAD", "CUST-404", OrderClassification.STANDARD, OrderStatus.PENDING, 39.99));
    }

    @Test
    void shouldFindOrdersByStoreId() {
        List<Order> orders = orderRepository.findByStoreId("STORE-LIS");
        assertThat(orders).hasSize(2);
    }

    @Test
    void shouldFindOrdersByCustomerId() {
        List<Order> orders = orderRepository.findByCustomerId("CUST-101");
        assertThat(orders).hasSize(1);
    }

    @Test
    void shouldFindOrdersByClassification() {
        List<Order> vip = orderRepository.findByClassification(OrderClassification.VIP);
        assertThat(vip).hasSize(1);
    }

    @Test
    void shouldCalculateTotalRevenueForProcessedOrders() {
        Double revenue = orderRepository.getTotalRevenue();
        assertThat(revenue).isEqualTo(59.97 + 650.0 + 1200.0);
    }

    @Test
    void shouldCountByClassification() {
        Long standardCount = orderRepository.countByClassification(OrderClassification.STANDARD);
        assertThat(standardCount).isEqualTo(1); // only PROCESSED ones
    }

    private Order order(String storeId, String customerId,
                        OrderClassification classification,
                        OrderStatus status, double finalPrice) {
        return Order.builder()
                .storeId(storeId)
                .customerId(customerId)
                .product("Test Product")
                .category(OrderCategory.BEDDING)
                .quantity(1)
                .unitPrice(finalPrice)
                .totalPrice(finalPrice)
                .discountApplied(0.0)
                .finalPrice(finalPrice)
                .classification(classification)
                .status(status)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
