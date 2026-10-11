package com.digitalbanking.api.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Digital Banking API",
                version = "1.0.0",
                description = """
                        API bancária para operações de clientes, contas e transações.

                        Funcionalidades disponíveis:
                        - Cadastro e consulta de clientes (validação de CPF e e-mail únicos).
                        - Abertura e consulta de contas bancárias.
                        - Depósitos e saques com controle transacional de saldo.
                        - Transferências entre contas com bloqueio pessimista (evita saldo negativo concorrente).
                        - Consulta de extrato paginado por conta.

                        Os valores monetários utilizam sempre 2 casas decimais (DECIMAL(19,2)).
                        Esta documentação é gerada automaticamente pelo springdoc-openapi.
                        """,
                contact = @Contact(
                        name = "Digital Banking API",
                        email = "contato@digitalbanking.example"
                ),
                license = @License(
                        name = "MIT",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Ambiente local de desenvolvimento")
        }
)
public class OpenApiConfig {
}
