package com.digitalbanking.api.entity;

import com.digitalbanking.api.entity.enums.TipoTransacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "transacoes")
public class Transacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoTransacao tipo;

    @NotNull
    @Positive
    @Column(name = "valor", nullable = false, precision = 19, scale = 2)
    private BigDecimal valor;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conta_origem_id",
            foreignKey = @ForeignKey(name = "fk_transacao_conta_origem")
    )
    private Conta contaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "conta_destino_id",
            foreignKey = @ForeignKey(name = "fk_transacao_conta_destino")
    )
    private Conta contaDestino;

    public Transacao() {
    }

    public Transacao(TipoTransacao tipo, BigDecimal valor, Conta contaOrigem, Conta contaDestino) {
        this.tipo = tipo;
        this.valor = valor;
        this.contaOrigem = contaOrigem;
        this.contaDestino = contaDestino;
    }

    @PrePersist
    void prePersist() {
        if (dataHora == null) {
            dataHora = LocalDateTime.now(ZoneOffset.UTC);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoTransacao getTipo() {
        return tipo;
    }

    public void setTipo(TipoTransacao tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public Conta getContaOrigem() {
        return contaOrigem;
    }

    public void setContaOrigem(Conta contaOrigem) {
        this.contaOrigem = contaOrigem;
    }

    public Conta getContaDestino() {
        return contaDestino;
    }

    public void setContaDestino(Conta contaDestino) {
        this.contaDestino = contaDestino;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Transacao other)) {
            return false;
        }
        return id != null && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Transacao{id=" + id + ", tipo=" + tipo + ", valor=" + valor + ", dataHora=" + dataHora + "}";
    }

}
