package com.fpo.vendas.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaleRequest(
        @NotNull(message = "Client ID is required.")
        Long clientId,

        @NotBlank(message = "Discount type (NO_DISCOUNT or TEN_PERCENT) is required.")
        String discountType,

        @NotEmpty(message = "The sale must contain at least one item.")
        @Valid
        List<SaleItemRequest> items
) {
}
