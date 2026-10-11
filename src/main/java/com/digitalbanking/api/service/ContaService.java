package com.digitalbanking.api.service;

import com.digitalbanking.api.dto.conta.ContaRequest;
import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.dto.operacao.TransferenciaRequest;
import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.entity.Conta;
import com.digitalbanking.api.entity.Transacao;
import com.digitalbanking.api.entity.enums.TipoTransacao;
import com.digitalbanking.api.exception.AccountNumberGenerationException;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.InvalidAmountException;
import com.digitalbanking.api.exception.InvalidTransferException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.repository.ClienteRepository;
import com.digitalbanking.api.repository.ContaRepository;
import com.digitalbanking.api.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private static final int TAMANHO_NUMERO_CONTA = 8;
    private static final int MAX_TENTATIVAS_NUMERO = 20;
    private static final int ESCALA_MONETARIA = 2;
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999999999999.99");

    private final ContaRepository contaRepository;
    private final ClienteRepository clienteRepository;
    private final TransacaoRepository transacaoRepository;

    public ContaService(
            ContaRepository contaRepository,
            ClienteRepository clienteRepository,
            TransacaoRepository transacaoRepository
    ) {
        this.contaRepository = contaRepository;
        this.clienteRepository = clienteRepository;
        this.transacaoRepository = transacaoRepository;
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Cliente cliente = clienteRepository.findById(request.clienteId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Cliente não encontrado: id " + request.clienteId()));

        Conta conta = new Conta(gerarNumeroUnico(), cliente);
        return ContaResponse.from(contaRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(Long id) {
        Conta conta = contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: id " + id));
        return ContaResponse.from(conta);
    }

    @Transactional(readOnly = true)
    public Page<TransacaoResponse> consultarExtrato(Long contaId, Pageable pageable) {
        if (!contaRepository.existsById(contaId)) {
            throw new ResourceNotFoundException("Conta não encontrada: id " + contaId);
        }
        return transacaoRepository
                .findByContaOrigemIdOrContaDestinoIdOrderByDataHoraDescIdDesc(contaId, contaId, pageable)
                .map(TransacaoResponse::from);
    }

    @Transactional
    public TransacaoResponse depositar(Long contaId, BigDecimal valor) {
        BigDecimal valorNormalizado = validarValor(valor);
        Conta conta = buscarComBloqueio(contaId);
        conta.setSaldo(conta.getSaldo().add(valorNormalizado));
        return registrar(TipoTransacao.DEPOSITO, valorNormalizado, null, conta);
    }

    @Transactional
    public TransacaoResponse sacar(Long contaId, BigDecimal valor) {
        BigDecimal valorNormalizado = validarValor(valor);
        Conta conta = buscarComBloqueio(contaId);

        if (conta.getSaldo().compareTo(valorNormalizado) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar o saque.");
        }

        conta.setSaldo(conta.getSaldo().subtract(valorNormalizado));
        return registrar(TipoTransacao.SAQUE, valorNormalizado, conta, null);
    }

    /**
     * Realiza a transferência de forma atômica (débito e crédito na mesma transação).
     * As contas são travadas com bloqueio pessimista (PESSIMISTIC_WRITE) em ordem crescente de id,
     * um design que visa evitar deadlock e impedir que operações simultâneas deixem o saldo negativo.
     *
     * <p>Esse comportamento é verificado por
     * {@code ContaServiceIntegrationTest.devePreservarSaldoTotalSobTransferenciasConcorrentes}, que
     * executa 4 threads realizando, em direções opostas, 25 transferências entre as mesmas duas
     * contas e afirma que a soma dos saldos permanece constante, que nenhum saldo fica negativo,
     * que não há deadlock (as threads concluem dentro do timeout) e que as 100 transações são
     * registradas. A atomicidade (rollback integral quando o registro da transação falha) é
     * coberta por {@code ContaServiceIntegrationTest.deveManterSaldosQuandoORegistroDaTransacaoFalhar}.
     *
     * <p>Escopo da comprovação: os testes acima rodam contra um banco H2 em memória, com um número
     * fixo de threads em um único processo/JVM. Não cobrem cenários de múltiplas instâncias da
     * aplicação, cargas de trabalho adversariais nem idempotência.
     *
     * <p>Idempotência para evitar transferências duplicadas: fica para uma próxima etapa.
     * Uma solução robusta exigiria persistir uma chave de idempotência (nova coluna/tabela), o que
     * alteraria o schema do banco — proibido neste momento (mantido ddl-auto=none).
     */
    @Transactional
    public TransacaoResponse transferir(TransferenciaRequest request) {
        if (request.contaOrigemId().equals(request.contaDestinoId())) {
            throw new InvalidTransferException("A conta de origem e a conta de destino devem ser diferentes.");
        }

        BigDecimal valorNormalizado = validarValor(request.valor());

        Long menorId = Math.min(request.contaOrigemId(), request.contaDestinoId());
        Long maiorId = Math.max(request.contaOrigemId(), request.contaDestinoId());

        Conta menor = buscarComBloqueio(menorId);
        Conta maior = buscarComBloqueio(maiorId);

        Conta origem = menorId.equals(request.contaOrigemId()) ? menor : maior;
        Conta destino = menorId.equals(request.contaDestinoId()) ? menor : maior;

        if (origem.getSaldo().compareTo(valorNormalizado) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar a transferência.");
        }

        origem.setSaldo(origem.getSaldo().subtract(valorNormalizado));
        destino.setSaldo(destino.getSaldo().add(valorNormalizado));
        return registrar(TipoTransacao.TRANSFERENCIA, valorNormalizado, origem, destino);
    }

    private TransacaoResponse registrar(TipoTransacao tipo, BigDecimal valor, Conta origem, Conta destino) {
        Transacao transacao = new Transacao(tipo, valor, origem, destino);
        return TransacaoResponse.from(transacaoRepository.save(transacao));
    }

    private Conta buscarComBloqueio(Long contaId) {
        return contaRepository.findByIdWithLock(contaId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: id " + contaId));
    }

    private BigDecimal validarValor(BigDecimal valor) {
        if (valor == null || valor.signum() <= 0) {
            throw new InvalidAmountException("O valor da operação deve ser maior que zero.");
        }
        if (valor.stripTrailingZeros().scale() > ESCALA_MONETARIA) {
            throw new InvalidAmountException("O valor da operação deve ter no máximo duas casas decimais.");
        }
        BigDecimal valorNormalizado = valor.setScale(ESCALA_MONETARIA, RoundingMode.HALF_EVEN);
        if (valorNormalizado.compareTo(VALOR_MAXIMO) > 0) {
            throw new InvalidAmountException("O valor da operação excede o limite máximo permitido.");
        }
        return valorNormalizado;
    }

    private String gerarNumeroUnico() {
        for (int tentativa = 0; tentativa < MAX_TENTATIVAS_NUMERO; tentativa++) {
            String numero = gerarNumero();
            if (!contaRepository.existsByNumero(numero)) {
                return numero;
            }
        }
        throw new AccountNumberGenerationException(
                "Não foi possível gerar um número de conta único. Tente novamente.");
    }

    private String gerarNumero() {
        int limite = (int) Math.pow(10, TAMANHO_NUMERO_CONTA);
        int numero = ThreadLocalRandom.current().nextInt(limite);
        return String.format("%0" + TAMANHO_NUMERO_CONTA + "d", numero);
    }

}
