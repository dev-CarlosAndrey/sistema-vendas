package com.fpo.vendas.strategy.impl;

import com.fpo.vendas.model.SaleItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TenPercentDiscountCalculatorTest {

    private final TenPercentDiscountCalculator calculator = new TenPercentDiscountCalculator();

    @Test
    void deveAplicarDezPorCentoDeDescontoSobreOSubtotal() {
        SaleItem item = SaleItem.builder().quantity(2).unitPrice(BigDecimal.valueOf(1500)).build();

        BigDecimal total = calculator.calculateTotal(List.of(item));

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(2700));
    }

    @Test
    void deveAplicarDescontoSobreMultiplosItensComQuantidadesDiferentes() {
        SaleItem item1 = SaleItem.builder().quantity(2).unitPrice(BigDecimal.valueOf(100)).build();
        SaleItem item2 = SaleItem.builder().quantity(3).unitPrice(BigDecimal.valueOf(50)).build();

        BigDecimal total = calculator.calculateTotal(List.of(item1, item2));

        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(315));
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
