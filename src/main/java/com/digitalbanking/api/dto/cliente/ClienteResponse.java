package com.digitalbanking.api.dto.cliente;

import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.util.PiiMasker;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ClienteResponse", description = "Representação de um cliente cadastrado (dados pessoais mascarados).")
public record ClienteResponse(
        @Schema(description = "Identificador único do cliente.", example = "1")
        Long id,

        @Schema(description = "Nome completo do cliente.", example = "Ana Silva")
        String nome,

        @Schema(description = "CPF mascarado do cliente.", example = "***.***.789-09")
        String cpf,

        @Schema(description = "E-mail mascarado do cliente.", example = "an***@email.com")
        String email
) {

    public static ClienteResponse from(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                PiiMasker.maskCpf(cliente.getCpf()),
                PiiMasker.maskEmail(cliente.getEmail())
        );
    }

}
