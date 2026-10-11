package com.digitalbanking.api.dto.operacao;

import com.digitalbanking.api.entity.Transacao;
import com.digitalbanking.api.entity.enums.TipoTransacao;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "TransacaoResponse", description = "Representação de uma transação registrada.")
public record TransacaoResponse(
        @Schema(description = "Identificador único da transação.", example = "10")
        Long id,

        @Schema(description = "Tipo da transação.", example = "DEPOSITO")
        TipoTransacao tipo,

        @Schema(description = "Valor da transação.", example = "50.00")
        BigDecimal valor,

        @Schema(description = "Data e hora em que a transação foi registrada.", example = "2026-10-11T17:51:00")
        LocalDateTime dataHora,

        @Schema(description = "Identificador da conta de origem (nulo em depósitos).", example = "1")
        Long contaOrigemId,

        @Schema(description = "Identificador da conta de destino (nulo em saques).", example = "1")
        Long contaDestinoId
) {

    public static TransacaoResponse from(Transacao transacao) {
        Long contaOrigemId = transacao.getContaOrigem() != null ? transacao.getContaOrigem().getId() : null;
        Long contaDestinoId = transacao.getContaDestino() != null ? transacao.getContaDestino().getId() : null;
        return new TransacaoResponse(
                transacao.getId(),
                transacao.getTipo(),
                transacao.getValor(),
                transacao.getDataHora(),
                contaOrigemId,
                contaDestinoId
        );
    }

}
