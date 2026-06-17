package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.ProductRequest;
import com.fpo.vendas.dto.response.ProductResponse;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.mapper.ProductMapper;
import com.fpo.vendas.model.Product;
import com.fpo.vendas.model.Stock;
import com.fpo.vendas.repository.ProductRepository;
import com.fpo.vendas.service.interfaces.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = productMapper.toEntity(request);

        // Business Rule: Automatically initialize empty stock for new products
        Stock stock = Stock.builder()
                .product(product)
                .quantity(0)
                .build();
        product.setStock(stock);

        Product savedProduct = productRepository.save(product);
        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found."));
        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product with ID " + id + " not found."));

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());

        return productMapper.toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cannot delete. Product with ID " + id + " not found.");
        }
        productRepository.deleteById(id);
    }
}
