package com.fpo.vendas.service.impl;

import com.fpo.vendas.dto.request.ClientRequest;
import com.fpo.vendas.dto.response.ClientResponse;
import com.fpo.vendas.exception.BusinessException;
import com.fpo.vendas.exception.ResourceNotFoundException;
import com.fpo.vendas.mapper.ClientMapper;
import com.fpo.vendas.model.Client;
import com.fpo.vendas.repository.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientServiceImpl clientService;

    private Client client;
    private ClientRequest request;

    @BeforeEach
    void setUp() {
        client = Client.builder()
                .id(1L)
                .name("João da Silva")
                .email("joao@email.com")
                .cpf("12345678909")
                .phone("11999999999")
                .build();

        request = new ClientRequest("João da Silva", "joao@email.com", "12345678909", "11999999999");
    }

    @Test
    void deveCriarClienteQuandoCpfEEmailSaoUnicos() {
        when(clientRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(clientRepository.existsByEmail(request.email())).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(
                new ClientResponse(1L, client.getName(), client.getEmail(), client.getCpf(), client.getPhone(), null));

        ClientResponse response = clientService.create(request);

        assertThat(response.id()).isEqualTo(1L);
        verify(clientRepository).save(client);
    }

    @Test
    void deveLancarBusinessExceptionQuandoCpfJaExiste() {
        when(clientRepository.existsByCpf(request.cpf())).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("CPF");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void deveLancarBusinessExceptionQuandoEmailJaExiste() {
        when(clientRepository.existsByCpf(request.cpf())).thenReturn(false);
        when(clientRepository.existsByEmail(request.email())).thenReturn(true);

        assertThatThrownBy(() -> clientService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("e-mail");

        verify(clientRepository, never()).save(any());
    }

    @Test
    void deveRetornarClienteQuandoEncontradoPorId() {
        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientMapper.toResponse(client)).thenReturn(
                new ClientResponse(1L, client.getName(), client.getEmail(), client.getCpf(), client.getPhone(), null));

        ClientResponse response = clientService.findById(1L);

        assertThat(response.id()).isEqualTo(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionQuandoClienteNaoEncontradoPorId() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.findById(1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRetornarListaVaziaQuandoNaoHaClientes() {
        when(clientRepository.findAll()).thenReturn(List.of());

        List<ClientResponse> response = clientService.findAll();

        assertThat(response).isEmpty();
    }

    @Test
    void deveRetornarListaDeClientesMapeados() {
        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(clientMapper.toResponse(client)).thenReturn(
                new ClientResponse(1L, client.getName(), client.getEmail(), client.getCpf(), client.getPhone(), null));

        List<ClientResponse> response = clientService.findAll();

        assertThat(response).hasSize(1);
    }

    @Test
    void deveAtualizarClienteQuandoCpfNaoPertenceAOutroCliente() {
        ClientRequest updateRequest = new ClientRequest("João Atualizado", "novo@email.com", "12345678909", "11888888888");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByCpf(updateRequest.cpf())).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponse(client)).thenReturn(
                new ClientResponse(1L, updateRequest.name(), updateRequest.email(), updateRequest.cpf(), updateRequest.phone(), null));

        ClientResponse response = clientService.update(1L, updateRequest);

        assertThat(response.name()).isEqualTo("João Atualizado");
        verify(clientRepository).save(client);
    }

    @Test
    void deveLancarBusinessExceptionQuandoCpfPertenceAOutroCliente() {
        Client outroCliente = Client.builder().id(2L).cpf("12345678909").build();
        ClientRequest updateRequest = new ClientRequest("João Atualizado", "novo@email.com", "12345678909", "11888888888");

        when(clientRepository.findById(1L)).thenReturn(Optional.of(client));
        when(clientRepository.findByCpf(updateRequest.cpf())).thenReturn(Optional.of(outroCliente));

        assertThatThrownBy(() -> clientService.update(1L, updateRequest))
                .isInstanceOf(BusinessException.class);

        verify(clientRepository, never()).save(any());
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoAtualizarClienteInexistente() {
        when(clientRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clientService.update(1L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveExcluirClienteQuandoExiste() {
        when(clientRepository.existsById(1L)).thenReturn(true);

        clientService.delete(1L);

        verify(clientRepository).deleteById(1L);
    }

    @Test
    void deveLancarResourceNotFoundExceptionAoExcluirClienteInexistente() {
        when(clientRepository.existsById(1L)).thenReturn(false);

        assertThatThrownBy(() -> clientService.delete(1L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(clientRepository, never()).deleteById(any());
    }
}
