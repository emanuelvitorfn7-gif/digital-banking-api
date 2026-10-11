package com.digitalbanking.api.dto.comum;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.List;

@Schema(name = "ApiErrorResponse", description = "Formato padrão de resposta de erro da API.")
public record ApiErrorResponse(
        @Schema(description = "Momento em que o erro ocorreu (UTC).", example = "2026-10-11T20:51:00Z")
        Instant timestamp,

        @Schema(description = "Código HTTP do erro.", example = "400")
        int status,

        @Schema(description = "Descrição curta do status HTTP.", example = "Bad Request")
        String error,

        @Schema(description = "Mensagem detalhada do erro.", example = "Dados de entrada inválidos.")
        String message,

        @Schema(description = "Lista de detalhes adicionais (ex.: erros de validação por campo).")
        List<String> details
) {

    public static ApiErrorResponse of(int status, String error, String message, List<String> details) {
        return new ApiErrorResponse(
                Instant.now(),
                status,
                error,
                message,
                details == null ? List.of() : List.copyOf(details)
        );
    }

}
