package com.fpo.vendas.service.interfaces;

import com.fpo.vendas.dto.request.StockUpdateRequest;
import com.fpo.vendas.dto.response.StockResponse;

import java.util.List;

public interface IStockService {
    StockResponse findByProductId(Long productId);
    StockResponse updateQuantity(Long productId, StockUpdateRequest request);
    void decrementStock(Long productId, Integer quantity);
    List<StockResponse> findAll();
}
