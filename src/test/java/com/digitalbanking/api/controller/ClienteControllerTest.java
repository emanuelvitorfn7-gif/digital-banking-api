package com.digitalbanking.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.digitalbanking.api.dto.cliente.ClienteRequest;
import com.digitalbanking.api.dto.cliente.ClienteResponse;
import com.digitalbanking.api.exception.DuplicateResourceException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.service.ClienteService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClienteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ClienteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClienteService clienteService;

    @Test
    void deveCriarClienteERetornar201ComLocation() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class)))
                .thenReturn(new ClienteResponse(1L, "Ana Silva", "***.***.789-09", "an***@email.com"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana Silva","cpf":"12345678909","email":"ana@email.com"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/clientes/1")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"))
                .andExpect(jsonPath("$.cpf").value("***.***.789-09"))
                .andExpect(jsonPath("$.email").value("an***@email.com"));
    }

    @Test
    void deveRetornar400QuandoDadosDeEntradaForemInvalidos() throws Exception {
        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","cpf":"12345678909","email":"email-invalido"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Dados de entrada inválidos."))
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(clienteService);
    }

    @Test
    void deveRetornar409QuandoCpfJaEstiverCadastrado() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class)))
                .thenThrow(new DuplicateResourceException("Já existe um cliente cadastrado com o CPF informado."));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","cpf":"12345678909","email":"ana@email.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Já existe um cliente cadastrado com o CPF informado."));
    }

    @Test
    void deveBuscarClientePorIdRetornando200() throws Exception {
        when(clienteService.buscarPorId(1L))
                .thenReturn(new ClienteResponse(1L, "Ana Silva", "***.***.789-09", "an***@email.com"));

        mockMvc.perform(get("/clientes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Ana Silva"));
    }

    @Test
    void deveRetornar404QuandoClienteNaoExistir() throws Exception {
        when(clienteService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Cliente não encontrado: id 99"));

        mockMvc.perform(get("/clientes/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Cliente não encontrado: id 99"));
    }

    @Test
    void deveRetornar409QuandoHouverViolacaoDeIntegridadeNoBanco() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry for key 'uk_cliente_cpf'"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","cpf":"12345678909","email":"ana@email.com"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(
                        "Conflito de dados: o recurso já existe ou viola uma restrição de integridade."));
    }

    @Test
    void deveRetornar500QuandoOcorrerErroInesperado() throws Exception {
        when(clienteService.criar(any(ClienteRequest.class)))
                .thenThrow(new IllegalStateException("falha inesperada simulada"));

        mockMvc.perform(post("/clientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Ana","cpf":"12345678909","email":"ana@email.com"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.message").value("Erro interno inesperado."));
    }

}
