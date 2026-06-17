package com.fpo.vendas.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductRequest(
        @NotBlank(message = "Product name is required.")
        String name,

        String description,

        @NotNull(message = "Price is required.")
        @Positive(message = "Price must be greater than zero.")
        BigDecimal price,

        @NotBlank(message = "Category is required.")
        String category,

        @NotBlank(message = "Product type (PERISHABLE or NON_PERISHABLE) is required.")
        String type,

        // Optional fields depending on the subtype (LSP/Flexibility)
        LocalDate expirationDate,
        Integer warrantyMonths
) {
}
