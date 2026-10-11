package com.digitalbanking.api.repository;

import com.digitalbanking.api.entity.Transacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {

    Page<Transacao> findByContaOrigemIdOrderByDataHoraDescIdDesc(
            Long contaOrigemId,
            Pageable pageable
    );

    Page<Transacao> findByContaDestinoIdOrderByDataHoraDescIdDesc(
            Long contaDestinoId,
            Pageable pageable
    );

    Page<Transacao> findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDescIdDesc(
            Long contaOrigemId,
            Long contaDestinoId,
            Pageable pageable
    );

}
