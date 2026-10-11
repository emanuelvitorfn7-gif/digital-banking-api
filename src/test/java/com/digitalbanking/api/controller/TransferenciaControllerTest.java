package com.digitalbanking.api.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.dto.operacao.TransferenciaRequest;
import com.digitalbanking.api.entity.enums.TipoTransacao;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.InvalidTransferException;
import com.digitalbanking.api.service.ContaService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransferenciaController.class)
@AutoConfigureMockMvc(addFilters = false)
class TransferenciaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ContaService contaService;

    @Test
    void deveTransferirRetornando200() throws Exception {
        when(contaService.transferir(any(TransferenciaRequest.class)))
                .thenReturn(new TransacaoResponse(10L, TipoTransacao.TRANSFERENCIA, new BigDecimal("30.00"),
                        LocalDateTime.now(), 1L, 2L));

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contaOrigemId":1,"contaDestinoId":2,"valor":30.00}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("TRANSFERENCIA"))
                .andExpect(jsonPath("$.valor").value(30.00))
                .andExpect(jsonPath("$.contaOrigemId").value(1))
                .andExpect(jsonPath("$.contaDestinoId").value(2));
    }

    @Test
    void deveRetornar400QuandoTransferirParaMesmaConta() throws Exception {
        when(contaService.transferir(any(TransferenciaRequest.class)))
                .thenThrow(new InvalidTransferException("A conta de origem e a conta de destino devem ser diferentes."));

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contaOrigemId":1,"contaDestinoId":1,"valor":10.00}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("A conta de origem e a conta de destino devem ser diferentes."));
    }

    @Test
    void deveRetornar409QuandoSaldoInsuficiente() throws Exception {
        when(contaService.transferir(any(TransferenciaRequest.class)))
                .thenThrow(new InsufficientBalanceException("Saldo insuficiente para realizar a transferência."));

        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contaOrigemId":1,"contaDestinoId":2,"valor":9999.00}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void deveRetornar400QuandoCamposObrigatoriosEstiveremAusentes() throws Exception {
        mockMvc.perform(post("/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"contaOrigemId":1,"contaDestinoId":2}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.details").isArray());

        verifyNoInteractions(contaService);
    }

    @Test
    void deveRetornar405QuandoMetodoNaoForSuportado() throws Exception {
        mockMvc.perform(get("/transferencias"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.message").value("Método HTTP não suportado para este recurso."));
    }

}
