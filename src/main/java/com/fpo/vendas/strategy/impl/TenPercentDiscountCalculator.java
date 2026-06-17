package com.fpo.vendas.strategy.impl;

import com.fpo.vendas.model.SaleItem;
import com.fpo.vendas.strategy.SalesCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component("TEN_PERCENT")
public class TenPercentDiscountCalculator implements SalesCalculator {
    @Override
    public BigDecimal calculateTotal(List<SaleItem> items) {
        if (items == null || items.isEmpty()) return BigDecimal.ZERO;

        BigDecimal subtotal = items.stream()
                .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Aplica o desconto de 10% multiplicando por 0.90
        return subtotal.multiply(BigDecimal.valueOf(0.90));
    }
}
