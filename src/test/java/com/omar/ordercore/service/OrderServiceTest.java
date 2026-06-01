package com.omar.ordercore.service;

import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.domain.valueobject.OrderCategory;
import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import com.omar.ordercore.dto.request.CreateOrderRequest;
import com.omar.ordercore.dto.request.UpdateOrderRequest;
import com.omar.ordercore.exception.OrderAlreadyProcessedException;
import com.omar.ordercore.exception.OrderNotFoundException;
import com.omar.ordercore.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest validRequest;
    private Order pendingOrder;

    @BeforeEach
    void setUp() {
        validRequest = CreateOrderRequest.builder()
                .storeId("STORE-LIS")
                .customerId("CUST-101")
                .product("Pillow Comfort Pro")
                .category("BEDDING")
                .quantity(3)
                .unitPrice(19.99)
                .build();

        pendingOrder = Order.builder()
                .id("order-123")
                .storeId("STORE-LIS")
                .customerId("CUST-101")
                .product("Pillow Comfort Pro")
                .category(OrderCategory.BEDDING)
                .quantity(3)
                .unitPrice(19.99)
                .status(OrderStatus.PENDING)
                .classification(OrderClassification.STANDARD)
                .createdAt(LocalDateTime.now())
                .build();
    }

    // ─── Create ─────────────────────────────────────────────────────────────

    @Test
    void shouldCreateOrderWithPendingStatus() {
        when(orderRepository.save(any())).thenReturn(pendingOrder);

        Order result = orderService.createOrder(validRequest);

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void shouldMapCategoryFromString() {
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.createOrder(validRequest);

        assertThat(result.getCategory()).isEqualTo(OrderCategory.BEDDING);
    }

    // ─── Process ────────────────────────────────────────────────────────────

    @Test
    void shouldProcessOrderAndCalculateTotals() {
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.processOrder("order-123");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.PROCESSED);
        assertThat(result.getTotalPrice()).isEqualTo(3 * 19.99);
        assertThat(result.getDiscountApplied()).isEqualTo(3 * 19.99 * 0.03); // BEDDING 3%
        assertThat(result.getFinalPrice()).isEqualTo(3 * 19.99 - (3 * 19.99 * 0.03));
        assertThat(result.getStoreCountry()).isEqualTo("Portugal");
        assertThat(result.getProcessedAt()).isNotNull();
    }

    @Test
    void shouldClassifyVipOrderCorrectly() {
        pendingOrder.setQuantity(1);
        pendingOrder.setUnitPrice(1200.0);
        pendingOrder.setCategory(OrderCategory.FURNITURE);

        when(orderRepository.findById("order-123")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.processOrder("order-123");

        assertThat(result.getClassification()).isEqualTo(OrderClassification.VIP);
    }

    @Test
    void shouldThrowWhenProcessingAlreadyProcessedOrder() {
        pendingOrder.setStatus(OrderStatus.PROCESSED);
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> orderService.processOrder("order-123"))
                .isInstanceOf(OrderAlreadyProcessedException.class)
                .hasMessageContaining("order-123");
    }

    // ─── Update ─────────────────────────────────────────────────────────────

    @Test
    void shouldUpdatePendingOrder() {
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        UpdateOrderRequest update = new UpdateOrderRequest(5, 24.99);
        Order result = orderService.updateOrder("order-123", update);

        assertThat(result.getQuantity()).isEqualTo(5);
        assertThat(result.getUnitPrice()).isEqualTo(24.99);
    }

    @Test
    void shouldThrowWhenUpdatingProcessedOrder() {
        pendingOrder.setStatus(OrderStatus.PROCESSED);
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(pendingOrder));

        assertThatThrownBy(() -> orderService.updateOrder("order-123", new UpdateOrderRequest(5, 24.99)))
                .isInstanceOf(OrderAlreadyProcessedException.class);
    }

    // ─── Cancel ─────────────────────────────────────────────────────────────

    @Test
    void shouldCancelPendingOrder() {
        when(orderRepository.findById("order-123")).thenReturn(Optional.of(pendingOrder));
        when(orderRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Order result = orderService.cancelOrder("order-123");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    // ─── Find ────────────────────────────────────────────────────────────────

    @Test
    void shouldThrowWhenOrderNotFound() {
        when(orderRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.findById("missing"))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
