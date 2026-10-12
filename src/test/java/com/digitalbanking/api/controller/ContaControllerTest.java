package com.digitalbanking.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.conta.SaldoResponse;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.service.ContaService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContaController.class)
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContaService contaService;

    @Test
    void deveCriarContaERetornar201ComLocation() throws Exception {
        when(contaService.criar(any()))
                .thenReturn(new ContaResponse(5L, "00000042", "Ana Silva", new BigDecimal("0.00")));

        mockMvc.perform(post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titular":"Ana Silva"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/contas/5")))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.numero").value("00000042"))
                .andExpect(jsonPath("$.titular").value("Ana Silva"))
                .andExpect(jsonPath("$.saldo").value(0.00));
    }

    @Test
    void deveRetornar400QuandoTitularNaoForInformado() throws Exception {
        mockMvc.perform(post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"titular":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(contaService);
    }

    @Test
    void deveListarContas() throws Exception {
        when(contaService.listar()).thenReturn(List.of(
                new ContaResponse(1L, "00000001", "Ana Silva", new BigDecimal("100.00")),
                new ContaResponse(2L, "00000002", "Bruno Souza", new BigDecimal("50.00"))));

        mockMvc.perform(get("/contas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].titular").value("Ana Silva"))
                .andExpect(jsonPath("$[1].titular").value("Bruno Souza"));
    }

    @Test
    void deveBuscarContaPorIdRetornando200() throws Exception {
        when(contaService.buscarPorId(1L))
                .thenReturn(new ContaResponse(1L, "00000001", "Ana Silva", new BigDecimal("100.00")));

        mockMvc.perform(get("/contas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numero").value("00000001"))
                .andExpect(jsonPath("$.saldo").value(100.00));
    }

    @Test
    void deveRetornar404QuandoContaNaoExistir() throws Exception {
        when(contaService.buscarPorId(99L))
                .thenThrow(new ResourceNotFoundException("Conta não encontrada: id 99"));

        mockMvc.perform(get("/contas/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveConsultarSaldo() throws Exception {
        when(contaService.consultarSaldo(1L))
                .thenReturn(new SaldoResponse(1L, "00000001", new BigDecimal("100.00")));

        mockMvc.perform(get("/contas/1/saldo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numero").value("00000001"))
                .andExpect(jsonPath("$.saldo").value(100.00));
    }

    @Test
    void deveDepositarRetornando200() throws Exception {
        when(contaService.depositar(eq(1L), any(BigDecimal.class)))
                .thenReturn(new ContaResponse(1L, "00000001", "Ana Silva", new BigDecimal("150.00")));

        mockMvc.perform(post("/contas/1/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":50.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saldo").value(150.00));
    }

    @Test
    void deveRetornar400AoDepositarValorNaoPositivo() throws Exception {
        mockMvc.perform(post("/contas/1/deposito")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(contaService);
    }

    @Test
    void deveRetornar409AoSacarComSaldoInsuficiente() throws Exception {
        when(contaService.sacar(eq(1L), any(BigDecimal.class)))
                .thenThrow(new InsufficientBalanceException("Saldo insuficiente para realizar o saque."));

        mockMvc.perform(post("/contas/1/saque")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":1000.00}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Saldo insuficiente para realizar o saque."));
    }

}
