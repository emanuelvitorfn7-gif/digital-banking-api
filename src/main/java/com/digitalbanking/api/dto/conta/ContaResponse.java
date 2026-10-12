package com.digitalbanking.api.dto.conta;

import com.digitalbanking.api.entity.Conta;
import java.math.BigDecimal;

public record ContaResponse(
        Long id,
        String numero,
        String titular,
        BigDecimal saldo
) {

    public static ContaResponse from(Conta conta) {
        return new ContaResponse(
                conta.getId(),
                conta.getNumero(),
                conta.getTitular(),
                conta.getSaldo()
        );
    }

}
