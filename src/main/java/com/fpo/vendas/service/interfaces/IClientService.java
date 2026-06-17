package com.fpo.vendas.service.interfaces;

import com.fpo.vendas.dto.request.ClientRequest;
import com.fpo.vendas.dto.response.ClientResponse;

import java.util.List;

public interface IClientService {
    ClientResponse create(ClientRequest request);
    ClientResponse findById(Long id);
    List<ClientResponse> findAll();
    ClientResponse update(Long id, ClientRequest request);
    void delete(Long id);
}
