package com.omar.ordercore.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Set;

public class StoreIdValidator implements ConstraintValidator<ValidStoreId, String> {

    private static final Set<String> VALID_STORE_IDS = Set.of(
            "STORE-LIS", "STORE-OPO", "STORE-MAD",
            "STORE-BCN", "STORE-CPH", "STORE-AAR"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;
        return VALID_STORE_IDS.contains(value.toUpperCase().trim());
    }
}
