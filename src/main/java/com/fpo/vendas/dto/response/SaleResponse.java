package com.fpo.vendas.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record SaleResponse(
        Long id,
        Long clientId,
        String clientName,
        LocalDateTime saleDate,
        BigDecimal totalValue,
        List<SaleItemResponse> items
) {
}
