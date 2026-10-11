package com.digitalbanking.api.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.digitalbanking.api.entity.enums.TipoTransacao;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class TransacaoTest {

    @Test
    void deveGerarDataHoraEmUtc() {
        Transacao transacao = new Transacao(TipoTransacao.DEPOSITO, new BigDecimal("10.00"), null, null);

        transacao.prePersist();

        LocalDateTime agoraUtc = LocalDateTime.now(ZoneOffset.UTC);
        assertThat(transacao.getDataHora()).isNotNull();
        assertThat(Duration.between(agoraUtc, transacao.getDataHora()).abs()).isLessThan(Duration.ofSeconds(5));
    }

    @Test
    void naoDeveSobrescreverDataHoraJaDefinida() {
        Transacao transacao = new Transacao(TipoTransacao.DEPOSITO, new BigDecimal("10.00"), null, null);
        LocalDateTime fixo = LocalDateTime.of(2020, 1, 1, 12, 0);
        transacao.setDataHora(fixo);

        transacao.prePersist();

        assertThat(transacao.getDataHora()).isEqualTo(fixo);
    }

}
