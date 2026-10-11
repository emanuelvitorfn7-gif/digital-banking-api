package com.digitalbanking.api.entity;

import com.digitalbanking.api.util.PiiMasker;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(
        name = "clientes",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_cliente_cpf", columnNames = "cpf"),
                @UniqueConstraint(name = "uk_cliente_email", columnNames = "email")
        }
)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nome", nullable = false, length = 120)
    private String nome;

    @NotBlank
    @Size(min = 11, max = 11)
    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(name = "email", nullable = false, length = 150, unique = true)
    private String email;

    public Cliente() {
    }

    public Cliente(String nome, String cpf, String email) {
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cliente other)) {
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
        return "Cliente{id=" + id + ", nome='" + nome + "', cpf='" + PiiMasker.maskCpf(cpf)
                + "', email='" + PiiMasker.maskEmail(email) + "'}";
    }

}
