package com.fpo.vendas.dto.validator.impl;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CPFValidatorTest {

    private final CPFValidator validator = new CPFValidator();

    @Test
    void deveRetornarTrueParaCpfValidoSemFormatacao() {
        assertThat(validator.isValid("12345678909", null)).isTrue();
    }

    @Test
    void deveRetornarTrueParaCpfValidoFormatado() {
        assertThat(validator.isValid("123.456.789-09", null)).isTrue();
    }

    @Test
    void deveRetornarFalseParaCpfComTodosDigitosIguais() {
        assertThat(validator.isValid("11111111111", null)).isFalse();
    }

    @Test
    void deveRetornarFalseParaCpfComTamanhoDiferenteDeOnze() {
        assertThat(validator.isValid("123456789", null)).isFalse();
    }

    @Test
    void deveRetornarFalseParaCpfComDigitoVerificadorInvalido() {
        assertThat(validator.isValid("12345678900", null)).isFalse();
    }

    @Test
    void deveRetornarTrueParaCpfNulo() {
        assertThat(validator.isValid(null, null)).isTrue();
    }
}
