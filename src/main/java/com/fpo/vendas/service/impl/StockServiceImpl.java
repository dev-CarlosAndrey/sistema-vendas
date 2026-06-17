package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.StockUpdateRequest;
import com.fpo.vendas.dto.response.StockResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.InsufficientStockException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.model.Stock;
import com.fpo.vendas.repository.StockRepository;
import com.fpo.vendas.service.interfaces.IStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements IStockService {

    private final StockRepository stockRepository;

    @Override
    @Transactional(readOnly = true)
    public StockResponse findByProductId(Long productId) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock record for product ID " + productId + " not found."));
        return new StockResponse(stock.getProduct().getId(), stock.getProduct().getName(), stock.getQuantity());
    }

    @Override
    @Transactional
    public StockResponse updateQuantity(Long productId, StockUpdateRequest request) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Stock record for product ID " + productId + " not found."));

        stock.setQuantity(request.quantity());
        Stock updated = stockRepository.save(stock);
        return new StockResponse(updated.getProduct().getId(), updated.getProduct().getName(), updated.getQuantity());
    }

    @Override
    @Transactional
    public void decrementStock(Long productId, Integer quantity) {
        Stock stock = stockRepository.findByProductId(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product ID " + productId + " has no stock registry."));

        // LSP Contract Verification: Polymorphic behavior checking for expiration dates or restrictions
        if (stock.getProduct().hasSalesRestriction()) {
            throw new BusinessException("O produto '" + stock.getProduct().getName() + "' possui restrições de venda (ex: Data de validade vencida).");
        }

        if (stock.getQuantity() < quantity) {
            throw new InsufficientStockException("Produto '" + stock.getProduct().getName() + "' possui apenas " + stock.getQuantity() + " unidade(s) em estoque.");
        }

        stock.setQuantity(stock.getQuantity() - quantity);
        stockRepository.save(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockResponse> findAll() {
        return stockRepository.findAll().stream()
                .map(s -> new StockResponse(s.getProduct().getId(), s.getProduct().getName(), s.getQuantity()))
                .toList();
    }
}
