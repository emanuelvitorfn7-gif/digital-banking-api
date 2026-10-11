package com.digitalbanking.api.dto.operacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "TransferenciaRequest", description = "Dados necessários para realizar uma transferência entre contas.")
public record TransferenciaRequest(

        @Schema(description = "Identificador da conta de origem (debitada).", example = "1")
        @NotNull(message = "A conta de origem é obrigatória.")
        @Positive(message = "A conta de origem deve ser positiva.")
        Long contaOrigemId,

        @Schema(description = "Identificador da conta de destino (creditada).", example = "2")
        @NotNull(message = "A conta de destino é obrigatória.")
        @Positive(message = "A conta de destino deve ser positiva.")
        Long contaDestinoId,

        @Schema(description = "Valor a ser transferido, maior que zero e com até 2 casas decimais.", example = "30.00")
        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
        BigDecimal valor

) {
}
