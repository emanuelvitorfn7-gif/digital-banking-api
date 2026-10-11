package com.digitalbanking.api.dto.cliente;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(name = "ClienteRequest", description = "Dados necessários para cadastrar um novo cliente.")
public record ClienteRequest(

        @Schema(description = "Nome completo do cliente.", example = "Ana Silva", maxLength = 120)
        @NotBlank(message = "O nome é obrigatório.")
        @Size(max = 120, message = "O nome deve ter no máximo 120 caracteres.")
        String nome,

        @Schema(description = "CPF do cliente, apenas números.", example = "12345678909", maxLength = 11)
        @NotBlank(message = "O CPF é obrigatório.")
        @Size(min = 11, max = 11, message = "O CPF informado é inválido.")
        String cpf,

        @Schema(description = "E-mail único do cliente.", example = "ana.silva@email.com", maxLength = 150)
        @NotBlank(message = "O e-mail é obrigatório.")
        @Email(message = "O e-mail informado é inválido.")
        @Size(max = 150, message = "O e-mail deve ter no máximo 150 caracteres.")
        String email

) {
}
