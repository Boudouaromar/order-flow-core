package com.omar.ordercore.exception;

public class OrderAlreadyProcessedException extends RuntimeException {
    public OrderAlreadyProcessedException(String id) {
        super("Order already processed: " + id);
    }
}
