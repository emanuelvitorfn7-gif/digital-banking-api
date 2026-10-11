package com.digitalbanking.api.dto.conta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(name = "ContaRequest", description = "Dados necessários para abrir uma nova conta bancária.")
public record ContaRequest(

        @Schema(description = "Identificador do cliente titular da conta.", example = "1")
        @NotNull(message = "O identificador do cliente é obrigatório.")
        @Positive(message = "O identificador do cliente deve ser positivo.")
        Long clienteId

) {
}
