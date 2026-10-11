package com.digitalbanking.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.dto.operacao.TransferenciaRequest;
import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.entity.Conta;
import com.digitalbanking.api.entity.Transacao;
import com.digitalbanking.api.repository.ClienteRepository;
import com.digitalbanking.api.repository.ContaRepository;
import com.digitalbanking.api.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:conta-fluxo;DB_CLOSE_DELAY=-1;LOCK_TIMEOUT=10000",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never"
        }
)
class ContaServiceIntegrationTest {

    @Autowired
    private ContaService contaService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ContaRepository contaRepository;

    @MockitoSpyBean
    private TransacaoRepository transacaoRepository;

    private Cliente cliente;

    @BeforeEach
    void preparar() {
        transacaoRepository.deleteAll();
        contaRepository.deleteAll();
        clienteRepository.deleteAll();
        cliente = clienteRepository.save(new Cliente("Titular Teste", "12345678909", "titular@example.com"));
    }

    @Test
    void devePreservarSaldoTotalSobTransferenciasConcorrentes() throws Exception {
        Conta contaA = criarConta("00000001", "1000.00");
        Conta contaB = criarConta("00000002", "1000.00");

        int totalThreads = 4;
        int transferenciasPorThread = 25;
        BigDecimal umReal = new BigDecimal("1.00");

        ExecutorService pool = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch inicio = new CountDownLatch(1);
        List<Future<?>> futuros = new ArrayList<>();

        for (int t = 0; t < totalThreads; t++) {
            final boolean deAParaB = t % 2 == 0;
            futuros.add(pool.submit(() -> {
                inicio.await();
                Long origem = deAParaB ? contaA.getId() : contaB.getId();
                Long destino = deAParaB ? contaB.getId() : contaA.getId();
                for (int i = 0; i < transferenciasPorThread; i++) {
                    contaService.transferir(new TransferenciaRequest(origem, destino, umReal));
                }
                return null;
            }));
        }

        inicio.countDown();
        for (Future<?> futuro : futuros) {
            futuro.get(60, TimeUnit.SECONDS);
        }
        pool.shutdown();

        assertThat(saldo(contaA.getId()).add(saldo(contaB.getId()))).isEqualByComparingTo("2000.00");
        assertThat(saldo(contaA.getId())).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(saldo(contaB.getId())).isGreaterThanOrEqualTo(BigDecimal.ZERO);
        assertThat(transacaoRepository.count()).isEqualTo((long) totalThreads * transferenciasPorThread);
    }

    @Test
    void deveManterSaldosQuandoORegistroDaTransacaoFalhar() {
        Conta origem = criarConta("00000010", "100.00");
        Conta destino = criarConta("00000011", "0.00");

        doThrow(new DataIntegrityViolationException("falha simulada ao registrar transacao"))
                .when(transacaoRepository).save(any(Transacao.class));

        assertThatThrownBy(() -> contaService.transferir(
                new TransferenciaRequest(origem.getId(), destino.getId(), new BigDecimal("30.00"))))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(saldo(origem.getId())).isEqualByComparingTo("100.00");
        assertThat(saldo(destino.getId())).isEqualByComparingTo("0.00");
        assertThat(transacaoRepository.count()).isZero();
    }

    @Test
    void saldoDeveSerConsistenteComOHistoricoDeTransacoes() {
        Conta contaA = criarConta("00000020", "0.00");
        Conta contaB = criarConta("00000021", "0.00");

        contaService.depositar(contaA.getId(), new BigDecimal("100.00"));
        contaService.sacar(contaA.getId(), new BigDecimal("20.00"));
        contaService.transferir(new TransferenciaRequest(contaA.getId(), contaB.getId(), new BigDecimal("30.00")));

        BigDecimal saldoCalculado = somarExtratoComSinal(contaA.getId());

        assertThat(saldo(contaA.getId())).isEqualByComparingTo("50.00");
        assertThat(saldo(contaA.getId())).isEqualByComparingTo(saldoCalculado);
    }

    private Conta criarConta(String numero, String saldoInicial) {
        Conta conta = new Conta(numero, cliente);
        conta.setSaldo(new BigDecimal(saldoInicial));
        return contaRepository.save(conta);
    }

    private BigDecimal saldo(Long contaId) {
        return contaRepository.findById(contaId).orElseThrow().getSaldo();
    }

    private BigDecimal somarExtratoComSinal(Long contaId) {
        Page<TransacaoResponse> pagina = contaService.consultarExtrato(contaId, PageRequest.of(0, 100));
        BigDecimal total = BigDecimal.ZERO;
        for (TransacaoResponse transacao : pagina.getContent()) {
            switch (transacao.tipo()) {
                case DEPOSITO -> total = total.add(transacao.valor());
                case SAQUE -> total = total.subtract(transacao.valor());
                case TRANSFERENCIA -> {
                    if (contaId.equals(transacao.contaOrigemId())) {
                        total = total.subtract(transacao.valor());
                    }
                    if (contaId.equals(transacao.contaDestinoId())) {
                        total = total.add(transacao.valor());
                    }
                }
            }
        }
        return total;
    }

}
