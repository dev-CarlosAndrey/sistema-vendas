package com.fpo.vendas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductRequest(
        @NotBlank(message = "O nome do produto é obrigatório.")
        String name,

        String description,

        @NotNull(message = "O preço é obrigatório.")
        @Positive(message = "O preço deve ser maior que zero.")
        BigDecimal price,

        @NotBlank(message = "A categoria é obrigatória.")
        String category,

        @NotBlank(message = "O tipo do produto (PERISHABLE ou NON_PERISHABLE) é obrigatório.")
        String type,

        // Campos opcionais dependendo do subtipo (LSP/Flexibilidade)
        LocalDate expirationDate,
        Integer warrantyMonths
) {
}