package com.digitalbanking.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.digitalbanking.api.dto.conta.ContaRequest;
import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.conta.SaldoResponse;
import com.digitalbanking.api.entity.Conta;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.InvalidAmountException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import com.digitalbanking.api.repository.ContaRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock
    private ContaRepository contaRepository;

    @InjectMocks
    private ContaService contaService;

    private Conta contaComSaldo(Long id, String numero, String saldo) {
        Conta conta = new Conta(numero, "Ana Silva");
        conta.setId(id);
        conta.setSaldo(new BigDecimal(saldo));
        return conta;
    }

    @Test
    void deveCriarContaComSaldoZero() {
        when(contaRepository.existsByNumero(anyString())).thenReturn(false);
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> {
            Conta conta = invocation.getArgument(0);
            conta.setId(5L);
            return conta;
        });

        ContaResponse response = contaService.criar(new ContaRequest("Ana Silva"));

        assertThat(response.id()).isEqualTo(5L);
        assertThat(response.numero()).isNotBlank();
        assertThat(response.titular()).isEqualTo("Ana Silva");
        assertThat(response.saldo()).isEqualByComparingTo("0.00");
    }

    @Test
    void deveGerarNovoNumeroQuandoHouverColisao() {
        when(contaRepository.existsByNumero(anyString())).thenReturn(true, true, false);
        when(contaRepository.save(any(Conta.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContaResponse response = contaService.criar(new ContaRequest("Ana Silva"));

        assertThat(response.numero()).isNotBlank();
        verify(contaRepository, times(3)).existsByNumero(anyString());
    }

    @Test
    void deveListarContasOrdenadasPorId() {
        when(contaRepository.findAll(any(Sort.class)))
                .thenReturn(List.of(contaComSaldo(1L, "00000001", "10.00"),
                        contaComSaldo(2L, "00000002", "20.00")));

        List<ContaResponse> contas = contaService.listar();

        assertThat(contas).hasSize(2);
        assertThat(contas.get(0).id()).isEqualTo(1L);
        assertThat(contas.get(1).id()).isEqualTo(2L);
    }

    @Test
    void deveBuscarContaPorId() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaComSaldo(1L, "00000001", "100.00")));

        ContaResponse response = contaService.buscarPorId(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.saldo()).isEqualByComparingTo("100.00");
    }

    @Test
    void deveLancarExcecaoQuandoContaNaoExistir() {
        when(contaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> contaService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveConsultarSaldo() {
        when(contaRepository.findById(1L)).thenReturn(Optional.of(contaComSaldo(1L, "00000001", "250.00")));

        SaldoResponse response = contaService.consultarSaldo(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.numero()).isEqualTo("00000001");
        assertThat(response.saldo()).isEqualByComparingTo("250.00");
    }

    @Test
    void deveDepositarSomandoAoSalso() {
        Conta conta = contaComSaldo(1L, "00000001", "100.00");
        when(contaRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(conta));

        ContaResponse response = contaService.depositar(1L, new BigDecimal("50.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("150.00");
        assertThat(response.saldo()).isEqualByComparingTo("150.00");
    }

    @Test
    void deveSacarSubtraindoSalso() {
        Conta conta = contaComSaldo(1L, "00000001", "100.00");
        when(contaRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(conta));

        ContaResponse response = contaService.sacar(1L, new BigDecimal("40.00"));

        assertThat(conta.getSaldo()).isEqualByComparingTo("60.00");
        assertThat(response.saldo()).isEqualByComparingTo("60.00");
    }

    @Test
    void deveRejeitarSaqueComSaldoInsuficiente() {
        Conta conta = contaComSaldo(1L, "00000001", "10.00");
        when(contaRepository.findByIdComBloqueio(1L)).thenReturn(Optional.of(conta));

        assertThatThrownBy(() -> contaService.sacar(1L, new BigDecimal("50.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(conta.getSaldo()).isEqualByComparingTo("10.00");
    }

    @Test
    void deveRejeitarValorNaoPositivo() {
        assertThatThrownBy(() -> contaService.depositar(1L, BigDecimal.ZERO))
                .isInstanceOf(InvalidAmountException.class);

        verifyNoInteractions(contaRepository);
    }

    @Test
    void deveRejeitarValorComMaisDeDuasCasasDecimais() {
        assertThatThrownBy(() -> contaService.depositar(1L, new BigDecimal("10.999")))
                .isInstanceOf(InvalidAmountException.class);

        verifyNoInteractions(contaRepository);
    }

}
