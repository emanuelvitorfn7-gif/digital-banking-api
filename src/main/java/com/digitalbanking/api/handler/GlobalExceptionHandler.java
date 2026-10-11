package com.digitalbanking.api.handler;

import com.digitalbanking.api.dto.comum.ApiErrorResponse;
import com.digitalbanking.api.exception.AccountNumberGenerationException;
import com.digitalbanking.api.exception.BusinessException;
import com.digitalbanking.api.exception.DuplicateResourceException;
import com.digitalbanking.api.exception.InsufficientBalanceException;
import com.digitalbanking.api.exception.InvalidAmountException;
import com.digitalbanking.api.exception.InvalidCpfException;
import com.digitalbanking.api.exception.InvalidTransferException;
import com.digitalbanking.api.exception.ResourceNotFoundException;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> tratarNaoEncontrado(ResourceNotFoundException ex) {
        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiErrorResponse> tratarDuplicidade(DuplicateResourceException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> tratarSaldoInsuficiente(InsufficientBalanceException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler(AccountNumberGenerationException.class)
    public ResponseEntity<ApiErrorResponse> tratarNumeroIndisponivel(AccountNumberGenerationException ex) {
        return construir(HttpStatus.CONFLICT, ex.getMessage(), List.of());
    }

    @ExceptionHandler({InvalidCpfException.class, InvalidAmountException.class, InvalidTransferException.class})
    public ResponseEntity<ApiErrorResponse> tratarRegraInvalida(BusinessException ex) {
        return construir(HttpStatus.BAD_REQUEST, ex.getMessage(), List.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarViolacaoIntegridade(DataIntegrityViolationException ex) {
        log.warn("Violação de integridade de dados: {}", ex.getMostSpecificCause().getMessage());
        return construir(HttpStatus.CONFLICT,
                "Conflito de dados: o recurso já existe ou viola uma restrição de integridade.", List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> tratarValidacao(MethodArgumentNotValidException ex) {
        List<String> detalhes = ex.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Dados de entrada inválidos.", detalhes);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse> tratarConstraintViolation(ConstraintViolationException ex) {
        List<String> detalhes = ex.getConstraintViolations().stream()
                .map(violacao -> violacao.getPropertyPath() + ": " + violacao.getMessage())
                .toList();
        return construir(HttpStatus.BAD_REQUEST, "Parâmetros de requisição inválidos.", detalhes);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> tratarCorpoInvalido(HttpMessageNotReadableException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Corpo da requisição ausente ou inválido.", List.of());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiErrorResponse> tratarParametroAusente(MissingServletRequestParameterException ex) {
        return construir(HttpStatus.BAD_REQUEST,
                "Parâmetro obrigatório ausente: " + ex.getParameterName(), List.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> tratarParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return construir(HttpStatus.BAD_REQUEST, "Parâmetro inválido: " + ex.getName(), List.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiErrorResponse> tratarMetodoNaoSuportado(HttpRequestMethodNotSupportedException ex) {
        log.warn("Método HTTP não suportado: {}", ex.getMethod());
        return construir(HttpStatus.METHOD_NOT_ALLOWED, "Método HTTP não suportado para este recurso.", List.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> tratarRecursoInexistente(NoResourceFoundException ex) {
        return construir(HttpStatus.NOT_FOUND, "Recurso não encontrado.", List.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> tratarErroInesperado(Exception ex) {
        log.error("Erro interno inesperado", ex);
        return construir(HttpStatus.INTERNAL_SERVER_ERROR, "Erro interno inesperado.", List.of());
    }

    private ResponseEntity<ApiErrorResponse> construir(HttpStatus status, String mensagem, List<String> detalhes) {
        ApiErrorResponse body = ApiErrorResponse.of(status.value(), status.getReasonPhrase(), mensagem, detalhes);
        return ResponseEntity.status(status).body(body);
    }

}
