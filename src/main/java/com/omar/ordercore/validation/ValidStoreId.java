package com.omar.ordercore.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StoreIdValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidStoreId {
    String message() default "Unknown storeId. Valid values: STORE-LIS, STORE-OPO, STORE-MAD, STORE-BCN, STORE-CPH, STORE-AAR";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
