package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.StockUpdateRequest;
import com.fpo.vendas.dto.response.StockResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.InsufficientStockException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.model.NonPerishableProduct;
import com.fpo.vendas.model.PerishableProduct;
import com.fpo.vendas.model.Product;
import com.fpo.vendas.model.Stock;
import com.fpo.vendas.repository.StockRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceImplTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockServiceImpl stockService;

    private Product buildProduct(boolean restricted) {
        if (restricted) {
            PerishableProduct product = new PerishableProduct();
            product.setId(1L);
            product.setName("Leite Integral UHT");
            product.setExpirationDate(LocalDate.now().minusDays(1));
            return product;
        }
        NonPerishableProduct product = new NonPerishableProduct();
        product.setId(1L);
        product.setName("Smartphone XYZ");
        return product;
    }

    @Test
    void deveRetornarEstoqueQuandoEncontrado() {
        Stock stock = Stock.builder().product(buildProduct(false)).quantity(10).build();
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        StockResponse response = stockService.findByProductId(1L);

        assertThat(response.quantity()).isEqualTo(10);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoEstoqueNaoEncontradoPorId() {
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.findByProductId(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveAtualizarQuantidadeDeEstoque() {
        Stock stock = Stock.builder().product(buildProduct(false)).quantity(10).build();
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));
        when(stockRepository.save(stock)).thenReturn(stock);

        StockResponse response = stockService.updateQuantity(1L, new StockUpdateRequest(50));

        assertThat(stock.getQuantity()).isEqualTo(50);
        assertThat(response.quantity()).isEqualTo(50);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarEstoqueInexistente() {
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.updateQuantity(1L, new StockUpdateRequest(50)))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveDecrementarEstoqueComSucesso() {
        Stock stock = Stock.builder().product(buildProduct(false)).quantity(10).build();
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        stockService.decrementStock(1L, 4);

        assertThat(stock.getQuantity()).isEqualTo(6);
        verify(stockRepository).save(stock);
    }

    @Test
    void deveLancarBusinessExceptionQuandoProdutoPereciveEstaVencido() {
        Stock stock = Stock.builder().product(buildProduct(true)).quantity(10).build();
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> stockService.decrementStock(1L, 1))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("restrições de venda");

        verify(stockRepository, never()).save(any());
    }

    @Test
    void deveLancarInsufficientStockExceptionQuandoQuantidadeSolicitadaMaiorQueEstoque() {
        Stock stock = Stock.builder().product(buildProduct(false)).quantity(2).build();
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.of(stock));

        assertThatThrownBy(() -> stockService.decrementStock(1L, 5))
                .isInstanceOf(InsufficientStockException.class);

        verify(stockRepository, never()).save(any());
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoProdutoNaoTemRegistroDeEstoque() {
        when(stockRepository.findByProductId(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> stockService.decrementStock(1L, 1))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRetornarListaDeEstoqueMapeada() {
        Stock stock = Stock.builder().product(buildProduct(false)).quantity(10).build();
        when(stockRepository.findAll()).thenReturn(List.of(stock));

        List<StockResponse> response = stockService.findAll();

        assertThat(response).hasSize(1);
        assertThat(response.get(0).quantity()).isEqualTo(10);
    }
}
