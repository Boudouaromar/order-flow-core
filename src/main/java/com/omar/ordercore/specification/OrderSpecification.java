package com.omar.ordercore.specification;

import com.omar.ordercore.domain.model.Order;
import com.omar.ordercore.dto.request.OrderFilterRequest;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {

    private OrderSpecification() {}

    public static Specification<Order> withFilter(OrderFilterRequest filter) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.getStoreId() != null && !filter.getStoreId().isBlank())
                predicates.add(cb.equal(root.get("storeId"), filter.getStoreId()));

            if (filter.getCustomerId() != null && !filter.getCustomerId().isBlank())
                predicates.add(cb.equal(root.get("customerId"), filter.getCustomerId()));

            if (filter.getStatus() != null)
                predicates.add(cb.equal(root.get("status"), filter.getStatus()));

            if (filter.getClassification() != null)
                predicates.add(cb.equal(root.get("classification"), filter.getClassification()));

            if (filter.getMinPrice() != null)
                predicates.add(cb.greaterThanOrEqualTo(root.get("finalPrice"), filter.getMinPrice()));

            if (filter.getMaxPrice() != null)
                predicates.add(cb.lessThanOrEqualTo(root.get("finalPrice"), filter.getMaxPrice()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
