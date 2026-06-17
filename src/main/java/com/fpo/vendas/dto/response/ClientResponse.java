package com.fpo.vendas.dto.response;

import java.time.LocalDateTime;

public record ClientResponse(
        Long id,
        String name,
        String email,
        String cpf,
        String phone,
        LocalDateTime registrationDate
) {
}
