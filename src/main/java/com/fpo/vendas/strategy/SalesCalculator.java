package com.fpo.vendas.strategy;

import com.fpo.vendas.model.SaleItem;

import java.math.BigDecimal;
import java.util.List;

public interface SalesCalculator {
    BigDecimal calculateTotal(List<SaleItem> items);
}
