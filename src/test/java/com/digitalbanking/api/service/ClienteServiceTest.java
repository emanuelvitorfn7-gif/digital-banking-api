package com.digitalbanking.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.digitalbanking.api.dto.cliente.ClienteRequest;
import com.digitalbanking.api.dto.cliente.ClienteResponse;
import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.exception.DuplicateResourceException;
import com.digitalbanking.api.exception.InvalidCpfException;
import com.digitalbanking.api.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deveCriarClienteComSucesso() {
        when(clienteRepository.existsByCpf("12345678909")).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> {
            Cliente cliente = invocation.getArgument(0);
            cliente.setId(1L);
            return cliente;
        });

        ClienteResponse response = clienteService.criar(
                new ClienteRequest("Ana Silva", "12345678909", "ana@email.com"));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.nome()).isEqualTo("Ana Silva");
        assertThat(response.cpf()).isEqualTo("***.***.789-09");
        assertThat(response.email()).isEqualTo("an***@email.com");
    }

    @Test
    void deveNormalizarCpfEEmailAntesDePersistir() {
        when(clienteRepository.existsByCpf("12345678909")).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@email.com")).thenReturn(false);
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteService.criar(
                new ClienteRequest("  Ana  ", "123.456.789-09", "  ANA@Email.com  "));

        assertThat(response.cpf()).isEqualTo("***.***.789-09");
        assertThat(response.email()).isEqualTo("an***@email.com");
        assertThat(response.nome()).isEqualTo("Ana");
    }

    @Test
    void deveRejeitarCpfComQuantidadeDeDigitosInvalida() {
        assertThatThrownBy(() -> clienteService.criar(
                new ClienteRequest("Ana", "123", "ana@email.com")))
                .isInstanceOf(InvalidCpfException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveRejeitarCpfComDigitoVerificadorInvalido() {
        assertThatThrownBy(() -> clienteService.criar(
                new ClienteRequest("Ana", "12345678900", "ana@email.com")))
                .isInstanceOf(InvalidCpfException.class);

        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).existsByCpf(anyString());
    }

    @Test
    void deveRejeitarCpfComTodosOsDigitosIguais() {
        assertThatThrownBy(() -> clienteService.criar(
                new ClienteRequest("Ana", "11111111111", "ana@email.com")))
                .isInstanceOf(InvalidCpfException.class);

        verify(clienteRepository, never()).save(any());
    }

    @Test
    void deveRejeitarCpfDuplicado() {
        when(clienteRepository.existsByCpf("12345678909")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(
                new ClienteRequest("Ana", "12345678909", "ana@email.com")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(clienteRepository, never()).save(any());
        verify(clienteRepository, never()).existsByEmail(anyString());
    }

    @Test
    void deveRejeitarEmailDuplicado() {
        when(clienteRepository.existsByCpf("12345678909")).thenReturn(false);
        when(clienteRepository.existsByEmail("ana@email.com")).thenReturn(true);

        assertThatThrownBy(() -> clienteService.criar(
                new ClienteRequest("Ana", "12345678909", "ana@email.com")))
                .isInstanceOf(DuplicateResourceException.class);

        verify(clienteRepository, never()).save(any());
    }

}
