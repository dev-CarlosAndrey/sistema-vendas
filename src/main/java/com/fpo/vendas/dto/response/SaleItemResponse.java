package com.fpo.vendas.dto.response;

import java.math.BigDecimal;

public record SaleItemResponse(
        Long productId,
        String productName,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}
