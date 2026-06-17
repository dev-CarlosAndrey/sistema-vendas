package com.fpo.vendas.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ClientRequest(
        @NotBlank(message = "Name is required.")
        String name,

        @NotBlank(message = "Email is required.")
        @Email(message = "Invalid email format.")
        String email,

        @NotBlank(message = "CPF is required.")
        @Pattern(regexp = "\\d{11}", message = "CPF must contain exactly 11 digits.")
        String cpf,

        String phone
) {
}
