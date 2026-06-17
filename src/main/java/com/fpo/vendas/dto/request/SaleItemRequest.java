package com.fpo.vendas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SaleItemRequest(
        @NotNull(message = "Product ID is required.")
        Long productId,

        @NotNull(message = "Quantity is required.")
        @Positive(message = "Quantity must be greater than zero.")
        Integer quantity
) {
}
