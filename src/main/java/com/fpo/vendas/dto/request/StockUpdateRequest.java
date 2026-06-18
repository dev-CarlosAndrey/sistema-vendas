package com.fpo.vendas.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockUpdateRequest(
        @NotNull(message = "A quantidade é obrigatória.")
        @PositiveOrZero(message = "A quantidade de estoque não pode ser negativa.")
        Integer quantity
) {
}