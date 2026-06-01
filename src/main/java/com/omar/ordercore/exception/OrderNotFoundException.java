package com.omar.ordercore.exception;

/**
 * Thrown when an order is not found by ID.
 * Extends RuntimeException — unchecked, callers are not forced to handle it.
 * The GlobalExceptionHandler maps this to a 404 response.
 */
public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(String id) {
        super("Order not found: " + id);
    }
}
