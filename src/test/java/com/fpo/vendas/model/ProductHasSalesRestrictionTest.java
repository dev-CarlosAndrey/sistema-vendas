package com.fpo.vendas.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class ProductHasSalesRestrictionTest {

    @Test
    void produtoNaoPerecivelNuncaTemRestricaoDeVenda() {
        NonPerishableProduct product = new NonPerishableProduct();

        assertThat(product.hasSalesRestriction()).isFalse();
    }

    @Test
    void produtoPerecivelComValidadeFuturaNaoTemRestricao() {
        PerishableProduct product = new PerishableProduct();
        product.setExpirationDate(LocalDate.now().plusDays(1));

        assertThat(product.hasSalesRestriction()).isFalse();
    }

    @Test
    void produtoPerecivelComValidadeVencidaTemRestricao() {
        PerishableProduct product = new PerishableProduct();
        product.setExpirationDate(LocalDate.now().minusDays(1));

        assertThat(product.hasSalesRestriction()).isTrue();
    }

    @Test
    void produtoPerecivelSemDataDeValidadeNaoTemRestricao() {
        PerishableProduct product = new PerishableProduct();
        product.setExpirationDate(null);

        assertThat(product.hasSalesRestriction()).isFalse();
    }
}
