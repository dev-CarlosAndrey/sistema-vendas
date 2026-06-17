package com.fpo.vendas.controller;

import com.fpo.vendas.dto.request.SaleRequest;
import com.fpo.vendas.dto.response.SaleResponse;
import com.fpo.vendas.service.interfaces.ISaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final ISaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> create(@Valid @RequestBody SaleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(saleService.executeSale(request));
    }

    @GetMapping
    public ResponseEntity<List<SaleResponse>> findAll() {
        return ResponseEntity.ok(saleService.findAll());
    }
}