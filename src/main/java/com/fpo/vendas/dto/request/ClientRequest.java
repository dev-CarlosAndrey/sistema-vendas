package com.fpo.vendas.dto.request;

import com.fpo.vendas.dto.validator.CPF;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ClientRequest(
        @NotBlank(message = "O nome é obrigatório.")
        String name,

        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O formato do e-mail é inválido.")
        String email,

        @NotBlank(message = "O CPF é obrigatório.")
        @CPF(message = "O CPF fornecido é matematicamente inválido.")
        String cpf,

        String phone
) {
}