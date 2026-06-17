package com.fpo.vendas.service.interfaces;

import com.fpo.vendas.dto.request.ProductRequest;
import com.fpo.vendas.dto.response.ProductResponse;

import java.util.List;

public interface IProductService {
    ProductResponse create(ProductRequest request);
    List<ProductResponse> findAll();
    ProductResponse findById(Long id);
    ProductResponse update(Long id, ProductRequest request);
    void delete(Long id);
}
