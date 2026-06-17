package com.fpo.vendas.dto.response;

public record StockResponse(
        Long productId,
        String productName,
        Integer quantity
) {
}
