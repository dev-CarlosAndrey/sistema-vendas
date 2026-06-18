package com.fpo.vendas.mapper;

import com.fpo.vendas.dto.request.SaleRequest;
import com.fpo.vendas.dto.response.SaleResponse;
import com.fpo.vendas.dto.response.SaleItemResponse;
import com.fpo.vendas.model.Sale;
import com.fpo.vendas.model.Client;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SaleMapper {

    public Sale toEntity(SaleRequest request, Client client) {
        if (request == null) return null;

        Sale sale = new Sale();
        sale.setClient(client);
        sale.setSaleDate(java.time.LocalDateTime.now());

        return sale;
    }

    public SaleResponse toResponse(Sale sale) {
        if (sale == null) return null;

        var itemsResponse = sale.getItems().stream()
                .map(item -> new SaleItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getSubtotal()
                ))
                .collect(Collectors.toList());

        return new SaleResponse(
                sale.getId(),
                sale.getClient().getId(),
                sale.getClient().getName(),
                sale.getSaleDate(),
                sale.getTotalValue(),
                itemsResponse
        );
    }
}