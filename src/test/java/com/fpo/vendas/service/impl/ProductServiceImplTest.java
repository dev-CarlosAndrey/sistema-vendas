package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.ProductRequest;
import com.fpo.vendas.dto.response.ProductResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.mapper.ProductMapper;
import com.fpo.vendas.model.NonPerishableProduct;
import com.fpo.vendas.model.PerishableProduct;
import com.fpo.vendas.model.Product;
import com.fpo.vendas.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void deveCriarProdutoNaoPereciveComGarantiaInformada() {
        ProductRequest request = new ProductRequest("Smartphone XYZ", "128GB", BigDecimal.valueOf(1500), "Eletrônicos",
                "NON_PERISHABLE", null, 12);
        NonPerishableProduct entity = new NonPerishableProduct();
        entity.setWarrantyMonths(12);

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponse(entity)).thenReturn(
                new ProductResponse(1L, request.name(), request.description(), request.price(), request.category(),
                        "NON_PERISHABLE", null, 12));

        ProductResponse response = productService.create(request);

        assertThat(response.warrantyMonths()).isEqualTo(12);
        verify(productRepository).save(entity);
    }

    @Test
    void deveLancarBusinessExceptionParaNaoPereciveSemGarantia() {
        ProductRequest request = new ProductRequest("Smartphone XYZ", "128GB", BigDecimal.valueOf(1500), "Eletrônicos",
                "NON_PERISHABLE", null, null);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("garantia");

        verify(productRepository, never()).save(any());
    }

    @Test
    void deveCriarProdutoPereciveComValidadeInformada() {
        LocalDate expiration = LocalDate.now().plusMonths(6);
        ProductRequest request = new ProductRequest("Leite Integral UHT", "Caixa 1L", BigDecimal.valueOf(4.50), "Alimentos",
                "PERISHABLE", expiration, null);
        PerishableProduct entity = new PerishableProduct();
        entity.setExpirationDate(expiration);

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponse(entity)).thenReturn(
                new ProductResponse(1L, request.name(), request.description(), request.price(), request.category(),
                        "PERISHABLE", expiration, null));

        ProductResponse response = productService.create(request);

        assertThat(response.expirationDate()).isEqualTo(expiration);
    }

    @Test
    void deveLancarBusinessExceptionParaPereciveSemValidade() {
        ProductRequest request = new ProductRequest("Leite Integral UHT", "Caixa 1L", BigDecimal.valueOf(4.50), "Alimentos",
                "PERISHABLE", null, null);

        assertThatThrownBy(() -> productService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("validade");

        verify(productRepository, never()).save(any());
    }

    @Test
    void deveInicializarEstoqueZeradoAoCriarProduto() {
        ProductRequest request = new ProductRequest("Smartphone XYZ", "128GB", BigDecimal.valueOf(1500), "Eletrônicos",
                "NON_PERISHABLE", null, 12);
        NonPerishableProduct entity = new NonPerishableProduct();

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(productMapper.toResponse(any())).thenReturn(
                new ProductResponse(1L, request.name(), request.description(), request.price(), request.category(),
                        "NON_PERISHABLE", null, 12));

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        productService.create(request);

        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getStock()).isNotNull();
        assertThat(captor.getValue().getStock().getQuantity()).isZero();
    }

    @Test
    void deveRetornarProdutoQuandoEncontradoPorId() {
        NonPerishableProduct entity = new NonPerishableProduct();
        entity.setId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productMapper.toResponse(entity)).thenReturn(
                new ProductResponse(1L, "Smartphone XYZ", null, BigDecimal.valueOf(1500), "Eletrônicos",
                        "NON_PERISHABLE", null, 12));

        ProductResponse response = productService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoProdutoNaoEncontradoPorId() {
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRetornarListaDeProdutosMapeados() {
        NonPerishableProduct entity = new NonPerishableProduct();
        when(productRepository.findAll()).thenReturn(List.of(entity));
        when(productMapper.toResponse(entity)).thenReturn(
                new ProductResponse(1L, "Smartphone XYZ", null, BigDecimal.valueOf(1500), "Eletrônicos",
                        "NON_PERISHABLE", null, 12));

        List<ProductResponse> response = productService.findAll();

        assertThat(response).hasSize(1);
    }

    @Test
    void deveAtualizarProdutoQuandoExiste() {
        NonPerishableProduct entity = new NonPerishableProduct();
        entity.setId(1L);
        ProductRequest request = new ProductRequest("Novo Nome", "Nova desc", BigDecimal.valueOf(2000), "Categoria",
                "NON_PERISHABLE", null, 12);

        when(productRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponse(entity)).thenReturn(
                new ProductResponse(1L, request.name(), request.description(), request.price(), request.category(),
                        "NON_PERISHABLE", null, 12));

        ProductResponse response = productService.update(1L, request);

        assertThat(response.name()).isEqualTo("Novo Nome");
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarProdutoInexistente() {
        ProductRequest request = new ProductRequest("Novo Nome", "Nova desc", BigDecimal.valueOf(2000), "Categoria",
                "NON_PERISHABLE", null, 12);
        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveExcluirProdutoQuandoExiste() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoExcluirProdutoInexistente() {
        when(productRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }
}
