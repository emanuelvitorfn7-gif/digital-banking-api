package com.digitalbanking.api.dto.conta;

import java.math.BigDecimal;

public record SaldoResponse(
        Long id,
        String numero,
        BigDecimal saldo
) {
}
