package com.digitalbanking.api.handler;

import static org.assertj.core.api.Assertions.assertThat;

import com.digitalbanking.api.dto.comum.ApiErrorResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveRetornar400QuandoParametroObrigatorioAusente() {
        ResponseEntity<ApiErrorResponse> resposta =
                handler.tratarParametroAusente(new MissingServletRequestParameterException("page", "int"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().status()).isEqualTo(400);
        assertThat(resposta.getBody().message()).contains("page");
    }

    @Test
    void deveRetornar400QuandoHouverViolacaoDeConstraint() {
        ResponseEntity<ApiErrorResponse> resposta =
                handler.tratarConstraintViolation(new ConstraintViolationException(Set.of()));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(resposta.getBody()).isNotNull();
        assertThat(resposta.getBody().status()).isEqualTo(400);
    }

}
