package com.digitalbanking.api.dto.cliente;

import static org.assertj.core.api.Assertions.assertThat;

import com.digitalbanking.api.entity.Cliente;
import org.junit.jupiter.api.Test;

class ClienteResponseTest {

    @Test
    void deveMascararCpfEEmail() {
        Cliente cliente = new Cliente("Ana Silva", "12345678909", "ana@email.com");
        cliente.setId(1L);

        ClienteResponse response = ClienteResponse.from(cliente);

        assertThat(response.cpf()).isEqualTo("***.***.789-09");
        assertThat(response.email()).isEqualTo("an***@email.com");
    }

    @Test
    void deveMascararEmailComLocalCurto() {
        Cliente cliente = new Cliente("Bia", "12345678909", "a@x.com");
        cliente.setId(2L);

        assertThat(ClienteResponse.from(cliente).email()).isEqualTo("a***@x.com");
    }

    @Test
    void devePreservarValoresAusentesOuInvalidos() {
        Cliente semDados = new Cliente("Sem Dados", "123", null);
        semDados.setId(3L);
        ClienteResponse responseInvalida = ClienteResponse.from(semDados);

        assertThat(responseInvalida.cpf()).isEqualTo("123");
        assertThat(responseInvalida.email()).isNull();

        Cliente semArroba = new Cliente("Sem Arroba", "12345678909", "semarroba");
        semArroba.setId(4L);

        assertThat(ClienteResponse.from(semArroba).email()).isEqualTo("***");
    }

}
