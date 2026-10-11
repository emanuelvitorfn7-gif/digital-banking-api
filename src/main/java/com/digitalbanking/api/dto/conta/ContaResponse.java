package com.digitalbanking.api.dto.conta;

import com.digitalbanking.api.entity.Conta;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(name = "ContaResponse", description = "Representação de uma conta bancária.")
public record ContaResponse(
        @Schema(description = "Identificador único da conta.", example = "1")
        Long id,

        @Schema(description = "Número da conta (8 dígitos, com zeros à esquerda).", example = "00000042")
        String numero,

        @Schema(description = "Saldo atual da conta.", example = "150.00")
        BigDecimal saldo,

        @Schema(description = "Identificador do cliente titular.", example = "1")
        Long clienteId,

        @Schema(description = "Nome do cliente titular.", example = "Ana Silva")
        String clienteNome
) {

    public static ContaResponse from(Conta conta) {
        Long clienteId = conta.getCliente() != null ? conta.getCliente().getId() : null;
        String clienteNome = conta.getCliente() != null ? conta.getCliente().getNome() : null;
        return new ContaResponse(
                conta.getId(),
                conta.getNumero(),
                conta.getSaldo(),
                clienteId,
                clienteNome
        );
    }

}
