package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.SaleRequest;
import com.fpo.vendas.dto.response.SaleItemResponse;
import com.fpo.vendas.dto.response.SaleResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.model.Client;
import com.fpo.vendas.model.Product;
import com.fpo.vendas.model.Sale;
import com.fpo.vendas.model.SaleItem;
import com.fpo.vendas.repository.ClientRepository;
import com.fpo.vendas.repository.ProductRepository;
import com.fpo.vendas.repository.SaleRepository;
import com.fpo.vendas.service.interfaces.ISaleService;
import com.fpo.vendas.service.interfaces.IStockService;
import com.fpo.vendas.strategy.SalesCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SaleServiceImpl implements ISaleService {

    private final SaleRepository saleRepository;
    private final ClientRepository clientRepository;
    private final ProductRepository productRepository;
    private final IStockService stockService;

    private final Map<String, SalesCalculator> calculatorStrategies;

    @Override
    @Transactional
    public SaleResponse executeSale(SaleRequest request) {
        // 1. Validar Cliente
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente com o ID " + request.clientId() + " não foi encontrado."));

        // 2. Validar Estratégia de Desconto
        SalesCalculator calculator = calculatorStrategies.get(request.discountType().toUpperCase());
        if (calculator == null) {
            throw new BusinessException("A estratégia de desconto '" + request.discountType() + "' não está implementada.");
        }

        // 3. Criar Cabeçalho da Venda
        Sale sale = Sale.builder()
                .client(client)
                .saleDate(LocalDateTime.now())
                .totalValue(BigDecimal.ZERO)
                .build();

        // 4. Processar itens, validar regras polimórficas (LSP) e dar baixa no estoque (SRP)
        for (var itemReq : request.items()) {
            Product product = productRepository.findById(itemReq.productId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produto com o ID " + itemReq.productId() + " não foi encontrado."));

            // Executa internamente o decremento de estoque e a validação se o produto está vencido
            stockService.decrementStock(product.getId(), itemReq.quantity());

            SaleItem item = SaleItem.builder()
                    .product(product)
                    .quantity(itemReq.quantity())
                    .unitPrice(product.getPrice()) // Salva o preço histórico da venda
                    .build();

            sale.addItem(item);
        }

        // 5. Calcula o total da venda aplicando a estratégia correta (OCP)
        BigDecimal totalCalculated = calculator.calculateTotal(sale.getItems());
        sale.setTotalValue(totalCalculated);

        // 6. Salva tudo em cascata no banco PostgreSQL
        Sale savedSale = saleRepository.save(sale);

        return mapToResponse(savedSale);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SaleResponse> findAll() {
        return saleRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    private SaleResponse mapToResponse(Sale sale) {
        List<SaleItemResponse> itemResponses = sale.getItems().stream()
                .map(item -> new SaleItemResponse(
                        item.getProduct().getId(),
                        item.getProduct().getName(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                )).toList();

        return new SaleResponse(
                sale.getId(),
                sale.getClient().getId(),
                sale.getClient().getName(),
                sale.getSaleDate(),
                sale.getTotalValue(),
                itemResponses
        );
    }
}