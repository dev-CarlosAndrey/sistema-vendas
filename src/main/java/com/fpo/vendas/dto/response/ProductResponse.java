package com.fpo.vendas.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String category,
        String type,
        LocalDate expirationDate,
        Integer warrantyMonths
) {
}
