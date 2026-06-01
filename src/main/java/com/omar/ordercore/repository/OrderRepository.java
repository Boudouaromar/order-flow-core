package com.omar.ordercore.repository;

import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.domain.valueobject.OrderClassification;
import com.omar.ordercore.domain.valueobject.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, String>,
        JpaSpecificationExecutor<Order> {

    Page<Order> findByStoreId(String storeId, Pageable pageable);
    List<Order> findByStoreId(String storeId);
    Page<Order> findByCustomerId(String customerId, Pageable pageable);
    List<Order> findByCustomerId(String customerId);
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);
    Page<Order> findByClassification(OrderClassification classification, Pageable pageable);
    List<Order> findByClassification(OrderClassification classification);

    @Query("SELECT SUM(o.finalPrice) FROM Order o WHERE o.status = 'PROCESSED'")
    Double getTotalRevenue();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.classification = :classification AND o.status = 'PROCESSED'")
    Long countByClassification(OrderClassification classification);
}
