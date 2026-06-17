package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.ClientRequest;
import com.fpo.vendas.dto.response.ClientResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.mapper.ClientMapper;
import com.fpo.vendas.model.Client;
import com.fpo.vendas.repository.ClientRepository;
import com.fpo.vendas.service.interfaces.IClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements IClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    @Override
    @Transactional
    public ClientResponse create(ClientRequest clientRequest) {
        if (clientRepository.existsByCpf(clientRequest.cpf())) {
            throw new BusinessException("A client with this CPF already exists.");
        }
        if (clientRepository.existsByEmail(clientRequest.email())) {
            throw new BusinessException("A client with this email already exists.");
        }

        Client client = clientMapper.toEntity(clientRequest);
        Client savedClient = clientRepository.save(client);
        return clientMapper.toResponse(savedClient);
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with ID " + id + " not found."));
        return clientMapper.toResponse(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponse> findAll() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ClientResponse update(Long id, ClientRequest request) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client with ID " + id + " not found."));

        // Validate duplicates excluding the current client's records
        clientRepository.findByCpf(request.cpf()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new BusinessException("This CPF is already taken by another client.");
            }
        });

        client.setName(request.name());
        client.setEmail(request.email());
        client.setCpf(request.cpf());
        client.setPhone(request.phone());

        return clientMapper.toResponse(clientRepository.save(client));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!clientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Client with ID " + id + " not found.");
        }
        clientRepository.deleteById(id);
    }
}
