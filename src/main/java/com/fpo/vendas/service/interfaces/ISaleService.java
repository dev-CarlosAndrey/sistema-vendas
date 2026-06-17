package com.fpo.vendas.service.interfaces;

import com.fpo.vendas.dto.request.SaleRequest;
import com.fpo.vendas.dto.response.SaleResponse;

import java.util.List;

public interface ISaleService {
    SaleResponse executeSale(SaleRequest request);
    List<SaleResponse> findAll();
}
