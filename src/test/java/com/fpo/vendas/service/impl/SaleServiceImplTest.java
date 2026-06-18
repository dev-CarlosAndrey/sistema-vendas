package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.SaleItemRequest;
import com.fpo.vendas.dto.request.SaleRequest;
import com.fpo.vendas.dto.response.SaleResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.InsufficientStockException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.mapper.SaleMapper;
import com.fpo.vendas.model.Client;
import com.fpo.vendas.model.NonPerishableProduct;
import com.fpo.vendas.model.Product;
import com.fpo.vendas.model.Sale;
import com.fpo.vendas.model.SaleItem;
import com.fpo.vendas.repository.ClientRepository;
import com.fpo.vendas.repository.ProductRepository;
import com.fpo.vendas.repository.SaleRepository;
import com.fpo.vendas.service.interfaces.IStockService;
import com.fpo.vendas.strategy.SalesCalculator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private IStockService stockService;

    @Mock
    private SalesCalculator noDiscountCalculator;

    private SaleServiceImpl saleService;

    private Client client;
    private Product product;

    @BeforeEach
    void setUp() {
        Map<String, SalesCalculator> strategies = Map.of("NO_DISCOUNT", noDiscountCalculator);
        saleService = new SaleServiceImpl(saleRepository, clientRepository, productRepository, stockService, new SaleMapper(), strategies);

        client = Client.builder().id(1L).name("João da Silva").build();

        product = new NonPerishableProduct();
        product.setId(1L);
        product.setName("Smartphone XYZ");
        product.setPrice(BigDecimal.valueOf(1500));
    }

    @Test
    void deveExecutarVendaComSucesso() {
        SaleRequest request = new SaleRequest(1L, "NO_DISCOUNT", List.of(new SaleItemRequest(1L, 2)));

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(noDiscountCalculator.calculateTotal(any())).thenReturn(BigDecimal.valueOf(3000));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale sale = invocation.getArgument(0);
            sale.setId(10L);
            return sale;
        });

        SaleResponse response = saleService.executeSale(request);

        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.totalValue()).isEqualTo(BigDecimal.valueOf(3000));
        assertThat(response.items()).hasSize(1);
        verify(stockService).decrementStock(1L, 2);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoClienteNaoExiste() {
        SaleRequest request = new SaleRequest(1L, "NO_DISCOUNT", List.of(new SaleItemRequest(1L, 2)));
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.executeSale(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(saleRepository, never()).save(any());
    }

    @Test
    void deveLancarBusinessExceptionQuandoEstrategiaDeDescontoNaoExiste() {
        SaleRequest request = new SaleRequest(1L, "BLACK_FRIDAY", List.of(new SaleItemRequest(1L, 2)));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThatThrownBy(() -> saleService.executeSale(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("BLACK_FRIDAY");

        verify(saleRepository, never()).save(any());
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoProdutoDoItemNaoExiste() {
        SaleRequest request = new SaleRequest(1L, "NO_DISCOUNT", List.of(new SaleItemRequest(1L, 2)));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.executeSale(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(saleRepository, never()).save(any());
    }

    @Test
    void devePropagarExcecaoDeEstoqueInsuficiente() {
        SaleRequest request = new SaleRequest(1L, "NO_DISCOUNT", List.of(new SaleItemRequest(1L, 100)));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        doThrow(new InsufficientStockException("Estoque insuficiente"))
                .when(stockService).decrementStock(1L, 100);

        assertThatThrownBy(() -> saleService.executeSale(request))
                .isInstanceOf(InsufficientStockException.class);

        verify(saleRepository, never()).save(any());
    }

    @Test
    void deveSalvarPrecoHistoricoDoProdutoNoItemDaVenda() {
        SaleRequest request = new SaleRequest(1L, "NO_DISCOUNT", List.of(new SaleItemRequest(1L, 1)));
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(noDiscountCalculator.calculateTotal(any())).thenReturn(BigDecimal.valueOf(1500));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaleResponse response = saleService.executeSale(request);

        assertThat(response.items().get(0).unitPrice()).isEqualTo(BigDecimal.valueOf(1500));
    }

    @Test
    void deveRetornarListaVaziaDeVendasQuandoNaoHaVendas() {
        when(saleRepository.findAll()).thenReturn(List.of());

        List<SaleResponse> response = saleService.findAll();

        assertThat(response).isEmpty();
    }

    @Test
    void deveRetornarListaDeVendasMapeada() {
        Sale sale = Sale.builder().id(1L).client(client).saleDate(java.time.LocalDateTime.now())
                .totalValue(BigDecimal.valueOf(1500)).build();
        SaleItem item = SaleItem.builder().product(product).quantity(1).unitPrice(BigDecimal.valueOf(1500)).build();
        sale.addItem(item);

        when(saleRepository.findAll()).thenReturn(List.of(sale));

        List<SaleResponse> response = saleService.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).items()).hasSize(1);
    }
}
