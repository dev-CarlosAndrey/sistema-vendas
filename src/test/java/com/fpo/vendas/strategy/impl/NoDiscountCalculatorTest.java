package com.fpo.vendas.strategy.impl;

import com.fpo.vendas.model.SaleItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NoDiscountCalculatorTest {

    private final NoDiscountCalculator calculator = new NoDiscountCalculator();

    @Test
    void deveSomarItensSemAplicarDesconto() {
        SaleItem item1 = SaleItem.builder().quantity(2).unitPrice(BigDecimal.valueOf(100)).build();
        SaleItem item2 = SaleItem.builder().quantity(1).unitPrice(BigDecimal.valueOf(50)).build();

        BigDecimal total = calculator.calculateTotal(List.of(item1, item2));

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(250));
    }

    @Test
    void deveRetornarZeroQuandoListaForVazia() {
        BigDecimal total = calculator.calculateTotal(List.of());

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void deveRetornarZeroQuandoListaForNula() {
        BigDecimal total = calculator.calculateTotal(null);

        assertThat(total).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
