package com.digitalbanking.api.controller;

import com.digitalbanking.api.dto.cliente.ClienteRequest;
import com.digitalbanking.api.dto.cliente.ClienteResponse;
import com.digitalbanking.api.dto.comum.ApiErrorResponse;
import com.digitalbanking.api.service.ClienteService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/clientes")
@Tag(name = "Clientes", description = "Cadastro e consulta de clientes do banco.")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @PostMapping
    @Operation(
            summary = "Cadastrar cliente",
            description = "Cadastra um novo cliente. O CPF e o e-mail devem ser únicos; "
                    + "tentativas de duplicidade retornam HTTP 409."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Dados do cliente a ser cadastrado.",
            required = true,
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ClienteRequest.class),
                    examples = @ExampleObject(
                            name = "Novo cliente",
                            value = """
                                    {
                                      "nome": "Ana Silva",
                                      "cpf": "12345678909",
                                      "email": "ana.silva@email.com"
                                    }
                                    """
                    )
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente cadastrado com sucesso. O cabeçalho 'Location' aponta para o recurso criado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class),
                            examples = @ExampleObject(
                                    name = "Cliente criado",
                                    value = """
                                            {
                                              "id": 1,
                                              "nome": "Ana Silva",
                                              "cpf": "***.***.789-09",
                                              "email": "an***@email.com"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados de entrada inválidos (campos ausentes ou formato incorreto).",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Já existe um cliente com o CPF ou e-mail informado.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ClienteResponse> criar(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.criar(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Buscar cliente por id",
            description = "Retorna os dados de um cliente a partir do seu identificador."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ClienteResponse.class),
                            examples = @ExampleObject(
                                    name = "Cliente",
                                    value = """
                                            {
                                              "id": 1,
                                              "nome": "Ana Silva",
                                              "cpf": "***.***.789-09",
                                              "email": "an***@email.com"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente não encontrado.",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<ClienteResponse> buscarPorId(
            @Parameter(description = "Identificador único do cliente.", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(clienteService.buscarPorId(id));
    }

}
