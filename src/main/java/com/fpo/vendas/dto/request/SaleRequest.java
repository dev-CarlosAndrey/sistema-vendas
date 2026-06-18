package com.fpo.vendas.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record SaleRequest(
        @NotNull(message = "O ID do cliente é obrigatório.")
        Long clientId,

        @NotBlank(message = "O tipo de desconto (NO_DISCOUNT ou TEN_PERCENT) é obrigatório.")
        String discountType,

        @NotEmpty(message = "A venda deve conter pelo menos um item.")
        @Valid
        List<SaleItemRequest> items
) {
}