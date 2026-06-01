package com.omar.ordercore.dto.request;

import com.omar.ordercore.validation.ValidStoreId;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {

    @NotBlank(message = "storeId is required")
    @ValidStoreId
    private String storeId;

    @NotBlank(message = "customerId is required")
    private String customerId;

    @NotBlank(message = "product is required")
    private String product;

    @NotBlank(message = "category is required")
    @Schema(allowableValues = {"BEDDING", "FURNITURE", "MATTRESSES", "TEXTILES", "LIGHTING"})
    private String category;

    @NotNull(message = "quantity is required")
    @Min(value = 1, message = "quantity must be at least 1")
    private Integer quantity;

    @NotNull(message = "unitPrice is required")
    @DecimalMin(value = "0.01", message = "unitPrice must be greater than 0")
    private Double unitPrice;
}
