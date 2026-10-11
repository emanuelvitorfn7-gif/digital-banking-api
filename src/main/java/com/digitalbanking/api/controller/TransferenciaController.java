package com.digitalbanking.api.controller;

import com.digitalbanking.api.dto.comum.ApiErrorResponse;
import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.dto.operacao.TransferenciaRequest;
import com.digitalbanking.api.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transferencias")
@Tag(name = "Transferências", description = "Transferência de valores entre contas.")
public class TransferenciaController {

    private final ContaService contaService;

    public TransferenciaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    @Operation(
            summary = "Transferir",
            description = "Transfere um valor entre duas contas distintas, debitando a conta de origem "
                    + "e creditando a conta de destino na mesma transação. As contas são bloqueadas "
                    + "(PESSIMISTIC_WRITE) em ordem crescente de id, um design que visa evitar saldo "
                    + "negativo e deadlock em operações concorrentes."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Contas de origem e destino e o valor a transferir.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = TransferenciaRequest.class),
                    examples = @ExampleObject(
                            name = "Transferência",
                            value = """
                                    {
                                      "contaOrigemId": 1,
                                      "contaDestinoId": 2,
                                      "valor": 30.00
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transferência realizada com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransacaoResponse.class),
                            examples = @ExampleObject(
                                    name = "Transação de transferência",
                                    value = """
                                            {
                                              "id": 12,
                                              "tipo": "TRANSFERENCIA",
                                              "valor": 30.00,
                                              "dataHora": "2026-10-11T18:30:00",
                                              "contaOrigemId": 1,
                                              "contaDestinoId": 2
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados inválidos ou contas de origem e destino iguais.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta de origem ou destino não encontrada.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Saldo insuficiente na conta de origem.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TransacaoResponse> transferir(@Valid @RequestBody TransferenciaRequest request) {
        return ResponseEntity.ok(contaService.transferir(request));
    }

}
