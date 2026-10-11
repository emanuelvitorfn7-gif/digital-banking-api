package com.digitalbanking.api.controller;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.entity.enums.TipoTransacao;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.service.ContaService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ContaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContaService contaService;

    @Test
    void deveCriarContaERetornar201ComLocation() throws Exception {
        when(contaService.criar(any()))
                .thenReturn(new ContaResponse(5L, "00000042", new BigDecimal("0.00"), 1L, "Ana Silva"));

        mockMvc.perform(post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/contas/5")))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.numero").value("00000042"))
                .andExpect(jsonPath("$.saldo").value(0.00))
                .andExpect(jsonPath("$.clienteId").value(1));
    }

    @Test
    void deveRetornar400QuandoClienteIdNaoForInformado() throws Exception {
        mockMvc.perform(post("/contas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"clienteId":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verifyNoInteractions(contaService);
    }

    @Test
    void deveBuscarContaPorIdRetornando200() throws Exception {
        when(contaService.buscarPorId(1L))
                .thenReturn(new ContaResponse(1L, "00000001", new BigDecimal("100.00"), 1L, "Ana Silva"));

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
    void deveDepositarRetornando200() throws Exception {
        when(contaService.depositar(eq(1L), any(BigDecimal.class)))
                .thenReturn(new TransacaoResponse(10L, TipoTransacao.DEPOSITO, new BigDecimal("50.00"),
                        LocalDateTime.now(), null, 1L));

        mockMvc.perform(post("/contas/1/depositos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":50.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.valor").value(50.00))
                .andExpect(jsonPath("$.contaDestinoId").value(1));
    }

    @Test
    void deveRetornar400AoDepositarValorNaoPositivo() throws Exception {
        mockMvc.perform(post("/contas/1/depositos")
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

        mockMvc.perform(post("/contas/1/saques")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"valor":1000.00}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Saldo insuficiente para realizar o saque."));
    }

    @Test
    void deveConsultarExtratoComPaginacao() throws Exception {
        List<TransacaoResponse> transacoes = List.of(
                new TransacaoResponse(2L, TipoTransacao.TRANSFERENCIA, new BigDecimal("30.00"),
                        LocalDateTime.now(), 1L, 2L),
                new TransacaoResponse(1L, TipoTransacao.DEPOSITO, new BigDecimal("50.00"),
                        LocalDateTime.now(), null, 1L)
        );
        Page<TransacaoResponse> pagina = new PageImpl<>(transacoes, PageRequest.of(0, 10), 2);

        when(contaService.consultarExtrato(eq(1L), any(Pageable.class))).thenReturn(pagina);

        mockMvc.perform(get("/contas/1/extrato").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.content[1].tipo").value("DEPOSITO"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1));
    }

    @Test
    void deveRetornar404AoConsultarExtratoDeContaInexistente() throws Exception {
        when(contaService.consultarExtrato(eq(99L), any(Pageable.class)))
                .thenThrow(new ResourceNotFoundException("Conta não encontrada: id 99"));

        mockMvc.perform(get("/contas/99/extrato"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void deveRetornar400QuandoParametroDePaginacaoForInvalido() throws Exception {
        mockMvc.perform(get("/contas/1/extrato").param("page", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Parâmetro inválido: page"));

        verifyNoInteractions(contaService);
    }

}
