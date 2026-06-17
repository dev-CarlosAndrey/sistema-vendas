package com.fpo.vendas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockUpdateRequest(
        @NotNull(message = "Quantity is required.")
        @PositiveOrZero(message = "Stock quantity cannot be negative.")
        Integer quantity
) {
}
