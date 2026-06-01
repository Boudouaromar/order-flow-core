package com.omar.ordercore.service;

import com.omar.ordercore.audit.OrderAuditEvent;
import com.omar.ordercore.audit.OrderAuditRepository;
import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.domain.valueobject.OrderCategory;
import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import com.omar.ordercore.dto.request.CreateOrderRequest;
import com.omar.ordercore.dto.request.OrderFilterRequest;
import com.omar.ordercore.dto.request.UpdateOrderRequest;
import com.omar.ordercore.exception.OrderAlreadyProcessedException;
import com.omar.ordercore.exception.OrderNotFoundException;
import com.omar.ordercore.repository.OrderRepository;
import com.omar.ordercore.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Map<OrderCategory, Double> CATEGORY_DISCOUNTS = Map.of(
            OrderCategory.FURNITURE, 0.05,
            OrderCategory.MATTRESSES, 0.08,
            OrderCategory.BEDDING, 0.03,
            OrderCategory.TEXTILES, 0.02,
            OrderCategory.LIGHTING, 0.0,
            OrderCategory.UNKNOWN, 0.0
    );

    private final OrderRepository orderRepository;
    private final OrderAuditRepository auditRepository;
    private final StoreService storeService;

    @Transactional
    @CacheEvict(value = "summary", allEntries = true)
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .storeId(request.getStoreId())
                .customerId(request.getCustomerId())
                .product(request.getProduct())
                .category(OrderCategory.fromString(request.getCategory()))
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .status(OrderStatus.PENDING)
                .classification(OrderClassification.STANDARD)
                .build();

        Order saved = orderRepository.save(order);
        recordAudit(saved.getId(), null, OrderStatus.PENDING, "API", "Order created");
        log.info("Order created [{}] store={} customer={}", saved.getId(), saved.getStoreId(), saved.getCustomerId());
        return saved;
    }

    @Transactional
    @CacheEvict(value = {"orders", "summary"}, key = "#id")
    public Order processOrder(String id) {
        Order order = findByIdFromDb(id);

        if (order.getStatus() == OrderStatus.PROCESSED) {
            throw new OrderAlreadyProcessedException(id);
        }

        OrderStatus previousStatus = order.getStatus();
        String country = storeService.getCountry(order.getStoreId());
        double totalPrice = order.getQuantity() * order.getUnitPrice();
        double discountRate = CATEGORY_DISCOUNTS.getOrDefault(order.getCategory(), 0.0);
        double discountAmount = totalPrice * discountRate;
        double finalPrice = totalPrice - discountAmount;

        order.setStoreCountry(country);
        order.setTotalPrice(totalPrice);
        order.setDiscountApplied(discountAmount);
        order.setFinalPrice(finalPrice);
        order.setClassification(OrderClassification.classify(finalPrice));
        order.setStatus(OrderStatus.PROCESSED);
        order.setProcessedAt(LocalDateTime.now());

        Order processed = orderRepository.save(order);
        recordAudit(id, previousStatus, OrderStatus.PROCESSED, "API",
                String.format("Processed: classification=%s finalPrice=%.2f",
                        processed.getClassification(), finalPrice));

        log.info("Order processed [{}] classification={} finalPrice={}", id, processed.getClassification(), finalPrice);
        return processed;
    }

    @Transactional
    @CacheEvict(value = "orders", key = "#id")
    public Order updateOrder(String id, UpdateOrderRequest request) {
        Order order = findByIdFromDb(id);

        if (order.getStatus() == OrderStatus.PROCESSED) {
            throw new OrderAlreadyProcessedException(id);
        }

        order.setQuantity(request.getQuantity());
        order.setUnitPrice(request.getUnitPrice());
        Order updated = orderRepository.save(order);
        recordAudit(id, order.getStatus(), order.getStatus(), "API",
                String.format("Updated: qty=%d price=%.2f", request.getQuantity(), request.getUnitPrice()));
        return updated;
    }

    @Transactional
    @CacheEvict(value = {"orders", "summary"}, key = "#id")
    public Order cancelOrder(String id) {
        Order order = findByIdFromDb(id);

        if (order.getStatus() == OrderStatus.PROCESSED) {
            throw new OrderAlreadyProcessedException(id);
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelled = orderRepository.save(order);
        recordAudit(id, previousStatus, OrderStatus.CANCELLED, "API", "Order cancelled");
        return cancelled;
    }

    /**
     * Cached order lookup — returns from cache on subsequent calls.
     * Cache is evicted on any write operation (process, update, cancel).
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "orders", key = "#id")
    public Order findById(String id) {
        log.debug("Cache miss for order [{}] — fetching from DB", id);
        return findByIdFromDb(id);
    }

    @Transactional(readOnly = true)
    public Page<Order> findAll(OrderFilterRequest filter, Pageable pageable) {
        return orderRepository.findAll(OrderSpecification.withFilter(filter), pageable);
    }

    @Transactional(readOnly = true)
    public List<OrderAuditEvent> getAuditHistory(String orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new OrderNotFoundException(orderId);
        }
        return auditRepository.findByOrderIdOrderByOccurredAtAsc(orderId);
    }

    // Stats can tolerate 30s staleness — avoids running aggregation on every request
    @Transactional(readOnly = true)
    @Cacheable(value = "summary", key = "'global'")
    public Map<String, Object> getSummary() {
        log.debug("Cache miss for summary — running aggregation query");
        Double revenue = orderRepository.getTotalRevenue();
        return Map.of(
                "totalOrders", orderRepository.count(),
                "totalRevenue", revenue != null ? revenue : 0.0,
                "standard", orderRepository.countByClassification(OrderClassification.STANDARD),
                "large", orderRepository.countByClassification(OrderClassification.LARGE),
                "vip", orderRepository.countByClassification(OrderClassification.VIP)
        );
    }

    // Direct DB fetch used internally by write methods to avoid stale cache reads
    private Order findByIdFromDb(String id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
    }

    private void recordAudit(String orderId, OrderStatus from, OrderStatus to,
                              String triggeredBy, String note) {
        auditRepository.save(OrderAuditEvent.builder()
                .orderId(orderId)
                .fromStatus(from)
                .toStatus(to)
                .triggeredBy(triggeredBy)
                .note(note)
                .build());
    }
}
