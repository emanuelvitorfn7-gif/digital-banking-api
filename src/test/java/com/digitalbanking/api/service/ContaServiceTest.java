package com.digitalbanking.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.digitalbanking.api.dto.conta.ContaRequest;
import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.dto.operacao.TransferenciaRequest;
import com.digitalbanking.api.entity.Cliente;
import com.digitalbanking.api.entity.Conta;
import com.digitalbanking.api.entity.Transacao;
import com.digitalbanking.api.entity.enums.TipoTransacao;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.InvalidAmountException;
import com.digitalbanking.api.exception.InvalidTransferException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.repository.ClienteRepository;
import com.digitalbanking.api.repository.ContaRepository;
import com.digitalbanking.api.repository.TransacaoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private TransacaoRepository transacaoRepository;

    @InjectMocks
    private ContaService contaService;

    private Cliente clienteComId(Long id) {
        Cliente cliente = new Cliente("Ana Silva", "12345678909", "ana@email.com");
        cliente.setId(id);
        return cliente;
    }

    private Conta contaComSaldo(Long id, String numero, String saldo) {
        Conta conta = new Conta(numero, clienteComId(10L));
        conta.setId(id);
        conta.setSaldo(new BigDecimal(saldo));
        return conta;
    }

    @Test
    void deveCriarContaComSaldoZeroParaClienteExistente() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteComId(1L)));
        when(contaRepository.existsByNumero(anyString())).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
            Conta conta = invocation.getArgument(0);
            conta.setId(5L);
            return conta;
        });

        ContaResponse response = contaService.criar(new ContaRequest(1L));

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.numero()).isNotBlank();
        assertThat(response.saldo()).isEqualByComparingTo("0.00");
        assertThat(response.clienteId()).isEqualTo(1L);
    }

    @Test
    void deveRejeitarCriacaoDeContaParaClienteInexistente() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.criar(new ContaRequest(99L)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(contaRepository, never()).save(any());
    }

    @Test
    void deveGerarNovoNumeroQuandoHouverColisao() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(clienteComId(1L)));
        when(contaRepository.existsByNumero(anyString())).thenReturn(true, true, false);
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponse response = contaService.criar(new ContaRequest(1L));

        assertThat(response.numero()).isNotBlank();
        verify(contaRepository, times(3)).existsByNumero(anyString());
    }

    @Test
    void deveDepositarComSucesso() {
        Conta conta = contaComSaldo(1L, "00000001", "100.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransacaoResponse response = contaService.depositar(1L, new BigDecimal("50.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("150.00");
        assertThat(response.tipo()).isEqualTo(TipoTransacao.DEPOSITO);
        assertThat(response.valor()).isEqualByComparingTo("50.00");
        assertThat(response.contaDestinoId()).isEqualTo(1L);
    }

    @Test
    void deveSacarComSucesso() {
        Conta conta = contaComSaldo(1L, "00000001", "100.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(conta));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransacaoResponse response = contaService.sacar(1L, new BigDecimal("40.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("60.00");
        assertThat(response.tipo()).isEqualTo(TipoTransacao.SAQUE);
        assertThat(response.contaOrigemId()).isEqualTo(1L);
    }

    @Test
    void deveRejeitarSaqueComSaldoInsuficiente() {
        Conta conta = contaComSaldo(1L, "00000001", "10.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.sacar(1L, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("10.00");
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarDepositoComValorNaoPositivo() {
        assertThatThrownBy(() -> contaService.depositar(1L, BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);

        verifyNoInteractions(contaRepository);
    }

    @Test
    void deveRejeitarValorAcimaDoLimiteMaximo() {
        assertThatThrownBy(() -> contaService.depositar(1L, new BigDecimal("100000000000000000.00")))
                .isInstanceOf(InvalidAmountException.class);

        verifyNoInteractions(contaRepository);
    }

    @Test
    void deveTransferirComSucesso() {
        Conta origem = contaComSaldo(1L, "00000001", "100.00");
        Conta destino = contaComSaldo(2L, "00000002", "20.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransacaoResponse response = contaService.transferir(
                new TransferenciaRequest(1L, 2L, new BigDecimal("30.00")));

        assertThat(origem.getSaldo()).isEqualByComparingTo("70.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("50.00");
        assertThat(response.tipo()).isEqualTo(TipoTransacao.TRANSFERENCIA);
        assertThat(response.contaOrigemId()).isEqualTo(1L);
        assertThat(response.contaDestinoId()).isEqualTo(2L);
    }

    @Test
    void deveBloquearContasEmOrdemCrescenteDeIdMesmoComOrigemMaior() {
        Conta origem = contaComSaldo(2L, "00000002", "100.00");
        Conta destino = contaComSaldo(1L, "00000001", "0.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(destino));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(origem));
        when(transacaoRepository.save(any(Transacao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        contaService.transferir(new TransferenciaRequest(2L, 1L, new BigDecimal("25.00")));

        InOrder ordemDeBloqueio = inOrder(contaRepository);
        ordemDeBloqueio.verify(contaRepository).findByIdWithLock(1L);
        ordemDeBloqueio.verify(contaRepository).findByIdWithLock(2L);
        assertThat(origem.getSaldo()).isEqualByComparingTo("75.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("25.00");
    }

    @Test
    void deveRejeitarTransferenciaParaMesmaConta() {
        assertThatThrownBy(() -> contaService.transferir(
                new TransferenciaRequest(1L, 1L, new BigDecimal("10.00"))))
                .isInstanceOf(InvalidTransferException.class);

        verifyNoInteractions(contaRepository);
    }

    @Test
    void deveRejeitarTransferenciaComSaldoInsuficiente() {
        Conta origem = contaComSaldo(1L, "00000001", "10.00");
        Conta destino = contaComSaldo(2L, "00000002", "0.00");
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.of(origem));
        when(contaRepository.findByIdWithLock(2L)).thenReturn(Optional.of(destino));

        assertThatThrownBy(() -> contaService.transferir(
                new TransferenciaRequest(1L, 2L, new BigDecimal("50.00"))))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(origem.getSaldo()).isEqualByComparingTo("10.00");
        assertThat(destino.getSaldo()).isEqualByComparingTo("0.00");
        verify(transacaoRepository, never()).save(any());
    }

    @Test
    void deveRejeitarTransferenciaComContaDeOrigemInexistente() {
        when(contaRepository.findByIdWithLock(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.transferir(
                new TransferenciaRequest(1L, 2L, new BigDecimal("10.00"))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

}
