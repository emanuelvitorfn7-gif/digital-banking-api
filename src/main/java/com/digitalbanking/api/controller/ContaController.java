package com.digitalbanking.api.controller;

import com.digitalbanking.api.dto.comum.ApiErrorResponse;
import com.digitalbanking.api.dto.comum.PageResponse;
import com.digitalbanking.api.dto.conta.ContaRequest;
import com.digitalbanking.api.dto.conta.ContaResponse;
import com.digitalbanking.api.dto.operacao.TransacaoResponse;
import com.digitalbanking.api.dto.operacao.ValorRequest;
import com.digitalbanking.api.service.ContaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/contas")
@Tag(name = "Contas", description = "Abertura de contas, depósitos, saques e extrato.")
public class ContaController {

    private static final int TAMANHO_MAXIMO_PAGINA = 100;

    private final ContaService contaService;

    public ContaController(ContaService contaService) {
        this.contaService = contaService;
    }

    @PostMapping
    @Operation(
            summary = "Abrir conta",
            description = "Abre uma nova conta para um cliente existente. "
                    + "O número da conta é gerado automaticamente e o saldo inicial é zero."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Identificador do cliente que será o titular da conta.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ContaRequest.class),
                    examples = @ExampleObject(
                            name = "Nova conta",
                            value = """
                                    {
                                      "clienteId": 1
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Conta aberta com sucesso. O cabeçalho 'Location' aponta para o recurso criado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContaResponse.class),
                            examples = @ExampleObject(
                                    name = "Conta criada",
                                    value = """
                                            {
                                              "id": 5,
                                              "numero": "00000042",
                                              "saldo": 0.00,
                                              "clienteId": 1,
                                              "clienteNome": "Ana Silva"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente informado não encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ContaResponse> criar(@Valid @RequestBody ContaRequest request) {
        ContaResponse response = contaService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar conta por id",
            description = "Retorna os dados de uma conta, incluindo o saldo atual, a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Conta encontrada.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ContaResponse.class),
                            examples = @ExampleObject(
                                    name = "Conta",
                                    value = """
                                            {
                                              "id": 1,
                                              "numero": "00000001",
                                              "saldo": 150.00,
                                              "clienteId": 1,
                                              "clienteNome": "Ana Silva"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ContaResponse> buscarPorId(
            @Parameter(description = "Identificador único da conta.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(contaService.buscarPorId(id));
    }

    @PostMapping("/{id}/depositos")
    @Operation(
            summary = "Depositar",
            description = "Credita um valor na conta informada e registra uma transação do tipo DEPOSITO. "
                    + "O valor deve ser maior que zero e ter no máximo 2 casas decimais."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Valor a ser depositado.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValorRequest.class),
                    examples = @ExampleObject(
                            name = "Depósito",
                            value = """
                                    {
                                      "valor": 50.00
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Depósito realizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransacaoResponse.class),
                            examples = @ExampleObject(
                                    name = "Transação de depósito",
                                    value = """
                                            {
                                              "id": 10,
                                              "tipo": "DEPOSITO",
                                              "valor": 50.00,
                                              "dataHora": "2026-10-11T17:51:00",
                                              "contaOrigemId": null,
                                              "contaDestinoId": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Valor inválido (ausente, não positivo ou com mais de 2 casas decimais).",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TransacaoResponse> depositar(
            @Parameter(description = "Identificador único da conta de destino.", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ValorRequest request
    ) {
        return ResponseEntity.ok(contaService.depositar(id, request.valor()));
    }

    @PostMapping("/{id}/saques")
    @Operation(
            summary = "Sacar",
            description = "Debita um valor da conta informada e registra uma transação do tipo SAQUE. "
                    + "O valor deve ser maior que zero e não pode exceder o saldo disponível."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Valor a ser sacado.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValorRequest.class),
                    examples = @ExampleObject(
                            name = "Saque",
                            value = """
                                    {
                                      "valor": 20.00
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Saque realizado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TransacaoResponse.class),
                            examples = @ExampleObject(
                                    name = "Transação de saque",
                                    value = """
                                            {
                                              "id": 11,
                                              "tipo": "SAQUE",
                                              "valor": 20.00,
                                              "dataHora": "2026-10-11T18:10:00",
                                              "contaOrigemId": 1,
                                              "contaDestinoId": null
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Valor inválido (ausente, não positivo ou com mais de 2 casas decimais).",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Saldo insuficiente para realizar o saque.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<TransacaoResponse> sacar(
            @Parameter(description = "Identificador único da conta de origem.", example = "1", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ValorRequest request
    ) {
        return ResponseEntity.ok(contaService.sacar(id, request.valor()));
    }

    @GetMapping("/{id}/extrato")
    @Operation(
            summary = "Consultar extrato",
            description = "Retorna o extrato paginado da conta, considerando transações em que ela "
                    + "aparece como origem ou destino, ordenadas da mais recente para a mais antiga."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Extrato retornado com sucesso.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PageResponse.class),
                            examples = @ExampleObject(
                                    name = "Extrato paginado",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "id": 11,
                                                  "tipo": "SAQUE",
                                                  "valor": 20.00,
                                                  "dataHora": "2026-10-11T18:10:00",
                                                  "contaOrigemId": 1,
                                                  "contaDestinoId": null
                                                },
                                                {
                                                  "id": 10,
                                                  "tipo": "DEPOSITO",
                                                  "valor": 50.00,
                                                  "dataHora": "2026-10-11T17:51:00",
                                                  "contaOrigemId": null,
                                                  "contaDestinoId": 1
                                                }
                                              ],
                                              "page": 0,
                                              "size": 10,
                                              "totalElements": 2,
                                              "totalPages": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Conta não encontrada.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<PageResponse<TransacaoResponse>> consultarExtrato(
            @Parameter(description = "Identificador único da conta.", example = "1", required = true)
            @PathVariable Long id,
            @Parameter(description = "Número da página (base zero).", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Quantidade de itens por página (1 a 100).", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), normalizarTamanho(size));
        return ResponseEntity.ok(PageResponse.from(contaService.consultarExtrato(id, pageable)));
    }

    private int normalizarTamanho(int size) {
        return Math.min(Math.max(size, 1), TAMANHO_MAXIMO_PAGINA);
    }

}
