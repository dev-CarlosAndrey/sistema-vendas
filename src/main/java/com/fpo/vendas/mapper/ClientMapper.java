package com.fpo.vendas.mapper;

import com.fpo.vendas.dto.request.ClientRequest;
import com.fpo.vendas.dto.response.ClientResponse;
import com.fpo.vendas.model.Client;
import org.springframework.stereotype.Component;

@Component
public class ClientMapper {

    public Client toEntity(ClientRequest request) {
        if (request == null) return null;

        return Client.builder()
                .name(request.name())
                .email(request.email())
                .cpf(request.cpf())
                .phone(request.phone())
                .build();
    }

    public ClientResponse toResponse(Client client) {
        if (client == null) return null;

        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getEmail(),
                client.getCpf(),
                client.getPhone(),
                client.getRegistrationDate()
        );
    }

}
