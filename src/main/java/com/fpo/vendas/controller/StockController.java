package com.fpo.vendas.controller;

import com.fpo.vendas.dto.request.StockUpdateRequest;
import com.fpo.vendas.dto.response.StockResponse;
import com.fpo.vendas.service.interfaces.IStockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final IStockService stockService;

    @GetMapping("/{productId}")
    public ResponseEntity<StockResponse> findByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(stockService.findByProductId(productId));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<StockResponse> updateQuantity(@PathVariable Long productId, @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(stockService.updateQuantity(productId, request));
    }

    @GetMapping
    public ResponseEntity<List<StockResponse>> findAll() {
        return ResponseEntity.ok(stockService.findAll());
    }
}