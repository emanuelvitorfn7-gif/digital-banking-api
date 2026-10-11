package com.digitalbanking.api.dto.operacao;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(name = "ValorRequest", description = "Valor monetário para operações de depósito e saque.")
public record ValorRequest(

        @Schema(description = "Valor da operação, maior que zero e com até 2 casas decimais.", example = "50.00")
        @NotNull(message = "O valor é obrigatório.")
        @Positive(message = "O valor deve ser maior que zero.")
        @Digits(integer = 17, fraction = 2, message = "O valor deve ter no máximo duas casas decimais.")
        BigDecimal valor

) {
}
