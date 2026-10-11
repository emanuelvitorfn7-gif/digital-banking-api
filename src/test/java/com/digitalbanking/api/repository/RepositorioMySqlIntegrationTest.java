package com.digitalbanking.api.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.entity.Conta;
import com.digitalbanking.api.entity.Transacao;
import com.digitalbanking.api.entity.enums.TipoTransacao;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.mysql.MySQLContainer;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=create-drop")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class RepositorioMySqlIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer mysql = new MySQLContainer("mysql:8.4.7");

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Test
    void deveRecuperarContaComBloqueioPessimista() {
        Cliente cliente = novoCliente("12345678909", "titular@example.com");
        Conta conta = novaConta("11111111", "100.00", cliente);

        Optional<Conta> encontrada = contaRepository.findByIdWithLock(conta.getId());

        assertThat(encontrada).isPresent();
        assertThat(encontrada.orElseThrow().getSaldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveOrdenarExtratoConsiderandoOrigemEDestino() {
        Cliente cliente = novoCliente("12345678909", "titular@example.com");
        Conta contaA = novaConta("00000001", "0.00", cliente);
        Conta contaB = novaConta("00000002", "0.00", cliente);
        Conta contaC = novaConta("00000003", "0.00", cliente);

        Transacao deposito = novaTransacao(TipoTransacao.DEPOSITO, null, contaA, "2026-01-01T10:00");
        Transacao saida = novaTransacao(TipoTransacao.TRANSFERENCIA, contaA, contaB, "2026-01-01T11:00");
        Transacao entrada = novaTransacao(TipoTransacao.TRANSFERENCIA, contaC, contaA, "2026-01-01T12:00");
        Transacao saque = novaTransacao(TipoTransacao.SAQUE, contaA, null, "2026-01-01T13:00");
        transacaoRepository.flush();

        Page<Transacao> extrato = transacaoRepository
                .findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDescIdDesc(
                        contaA.getId(), contaA.getId(), PageRequest.of(0, 10));

        assertThat(extrato.getTotalElements()).isEqualTo(4);
        assertThat(extrato.getContent())
                .extracting(Transacao::getId)
                .containsExactly(saque.getId(), entrada.getId(), saida.getId(), deposito.getId());

        Page<Transacao> apenasOrigem = transacaoRepository
                .findByContaOrigemIdOrderByDataHoraDescIdDesc(contaA.getId(), PageRequest.of(0, 10));
        assertThat(apenasOrigem.getContent())
                .extracting(Transacao::getId)
                .containsExactly(saque.getId(), saida.getId());

        Page<Transacao> apenasDestino = transacaoRepository
                .findByContaDestinoIdOrderByDataHoraDescIdDesc(contaA.getId(), PageRequest.of(0, 10));
        assertThat(apenasDestino.getContent())
                .extracting(Transacao::getId)
                .containsExactly(entrada.getId(), deposito.getId());
    }

    @Test
    void deveImpedirCpfDuplicado() {
        clienteRepository.saveAndFlush(new Cliente("Ana", "12345678909", "ana@example.com"));

        assertThatThrownBy(() -> clienteRepository.saveAndFlush(
                new Cliente("Bruno", "12345678909", "bruno@example.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deveImpedirNumeroDeContaDuplicado() {
        Cliente cliente = novoCliente("12345678909", "titular@example.com");
        novaConta("99999999", "0.00", cliente);

        assertThatThrownBy(() -> contaRepository.saveAndFlush(new Conta("99999999", cliente)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Cliente novoCliente(String cpf, String email) {
        return clienteRepository.saveAndFlush(new Cliente("Titular", cpf, email));
    }

    private Conta novaConta(String numero, String saldo, Cliente cliente) {
        Conta conta = new Conta(numero, cliente);
        conta.setSaldo(new BigDecimal(saldo));
        return contaRepository.saveAndFlush(conta);
    }

    private Transacao novaTransacao(TipoTransacao tipo, Conta origem, Conta destino, String dataHora) {
        Transacao transacao = new Transacao(tipo, new BigDecimal("10.00"), origem, destino);
        transacao.setDataHora(LocalDateTime.parse(dataHora));
        return transacaoRepository.saveAndFlush(transacao);
    }

}
