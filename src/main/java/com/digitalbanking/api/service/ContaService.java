package com.digitalbanking.api.service;

import com.digitalbanking.api.dto.conta.ContaRequest;
import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.conta.SaldoResponse;
import com.digitalbanking.api.entity.Conta;
import com.digitalbanking.api.exception.AccountNumberGenerationException;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.InvalidAmountException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.repository.ContaRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContaService {

    private static final int TAMANHO_NUMERO_CONTA = 8;
    private static final int MAX_TENTATIVAS_NUMERO = 20;
    private static final int ESCALA_MONETARIA = 2;
    private static final BigDecimal VALOR_MAXIMO = new BigDecimal("99999999999999999.99");

    private final ContaRepository contaRepository;

    public ContaService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Transactional
    public ContaResponse criar(ContaRequest request) {
        Conta conta = new Conta(gerarNumeroUnico(), request.titular().trim());
        return ContaResponse.from(contaRepository.save(conta));
    }

    @Transactional(readOnly = true)
    public List<ContaResponse> listar() {
        return contaRepository.findAll(Sort.by("id")).stream()
                .map(ContaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ContaResponse buscarPorId(Long id) {
        return ContaResponse.from(buscarConta(id));
    }

    @Transactional(readOnly = true)
    public SaldoResponse consultarSaldo(Long id) {
        Conta conta = buscarConta(id);
        return new SaldoResponse(conta.getId(), conta.getNumero(), conta.getSaldo());
    }

    @Transactional
    public ContaResponse depositar(Long contaId, BigDecimal valor) {
        BigDecimal valorNormalizado = validarValor(valor);
        Conta conta = buscarComBloqueio(contaId);
        conta.setSaldo(conta.getSaldo().add(valorNormalizado));
        return ContaResponse.from(conta);
    }

    @Transactional
    public ContaResponse sacar(Long contaId, BigDecimal valor) {
        BigDecimal valorNormalizado = validarValor(valor);
        Conta conta = buscarComBloqueio(contaId);

        if (conta.getSaldo().compareTo(valorNormalizado) < 0) {
            throw new InsufficientBalanceException("Saldo insuficiente para realizar o saque.");
        }

        conta.setSaldo(conta.getSaldo().subtract(valorNormalizado));
        return ContaResponse.from(conta);
    }

    private Conta buscarConta(Long id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: id " + id));
    }

    private Conta buscarComBloqueio(Long id) {
        return contaRepository.findByIdComBloqueio(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada: id " + id));
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
