package com.digitalbanking.api.controller;

import com.digitalbanking.api.dto.conta.ContaRequest;
import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.conta.SaldoResponse;
import com.digitalbanking.api.dto.operacao.ValorRequest;
import com.digitalbanking.api.service.ContaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/contas")
public class ContaController {

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        ContaResponse response = contaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ContaResponse>> listar() {
        return ResponseEntity.ok(contaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<SaldoResponse> consultarSaldo(@PathVariable Long id) {
        return ResponseEntity.ok(contaService.consultarSaldo(id));
    }

    @PostMapping("/{id}/deposito")
    public ResponseEntity<ContaResponse> depositar(@PathVariable Long id, @Valid @RequestBody ValorRequest request) {
        return ResponseEntity.ok(contaService.depositar(id, request.valor()));
    }

    @PostMapping("/{id}/saque")
    public ResponseEntity<ContaResponse> sacar(@PathVariable Long id, @Valid @RequestBody ValorRequest request) {
        return ResponseEntity.ok(contaService.sacar(id, request.valor()));
    }

}
