package com.omar.ordercore.domain.valueobject;

public enum OrderCategory {
    BEDDING, FURNITURE, MATTRESSES, TEXTILES, LIGHTING, UNKNOWN;

    public static OrderCategory fromString(String value) {
        if (value == null || value.isBlank()) return UNKNOWN;
        try {
            return valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return UNKNOWN;
        }
    }
}
