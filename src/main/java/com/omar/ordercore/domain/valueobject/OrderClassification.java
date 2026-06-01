package com.omar.ordercore.domain.valueobject;

public enum OrderClassification {
    STANDARD, LARGE, VIP;

    private static final double LARGE_THRESHOLD = 500.0;
    private static final double VIP_THRESHOLD = 1000.0;

    public static OrderClassification classify(double totalPrice) {
        if (totalPrice >= VIP_THRESHOLD) return VIP;
        if (totalPrice >= LARGE_THRESHOLD) return LARGE;
        return STANDARD;
    }
}
