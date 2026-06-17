package com.fpo.vendas.strategy.impl;

import com.fpo.vendas.model.SaleItem;
import com.fpo.vendas.strategy.SalesCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("NO_DISCOUNT")
public class NoDiscountCalculator implements SalesCalculator {
    @Override
    public BigDecimal calculateTotal(List<SaleItem> items) {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;
        return items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
