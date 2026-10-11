package com.digitalbanking.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:security-it;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never",
                "app.security.admin.username=admin",
                "app.security.admin.password=admin-secret",
                "app.security.client.username=cliente",
                "app.security.client.password=cliente-secret"
        }
)
class SecurityIntegrationTest {

    private static final String CLIENTE_JSON =
            "{\"nome\":\"Ana\",\"cpf\":\"12345678909\",\"email\":\"ana@example.com\"}";

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Test
    void deveRetornar401SemCredenciais() throws Exception {
        HttpResponse<String> resposta = enviar(HttpMethod.POST, "/clientes", CLIENTE_JSON, null, null);

        assertThat(resposta.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void deveRetornar403QuandoPapelForInsuficiente() throws Exception {
        HttpResponse<String> resposta = enviar(HttpMethod.POST, "/clientes", CLIENTE_JSON, "cliente", "cliente-secret");

        assertThat(resposta.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void deveCriarClienteComAdministradorERetornarCpfMascarado() throws Exception {
        HttpResponse<String> resposta = enviar(HttpMethod.POST, "/clientes", CLIENTE_JSON, "admin", "admin-secret");

        assertThat(resposta.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(resposta.body()).contains("***.***.789-09");
        assertThat(resposta.body()).doesNotContain("12345678909");
    }

    @Test
    void devePermitirLeituraAutenticadaMasNaoAnonima() throws Exception {
        HttpResponse<String> anonima = enviar(HttpMethod.GET, "/contas/999", null, null, null);
        assertThat(anonima.statusCode()).isEqualTo(HttpStatus.UNAUTHORIZED.value());

        HttpResponse<String> autenticada = enviar(HttpMethod.GET, "/contas/999", null, "cliente", "cliente-secret");
        assertThat(autenticada.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deveExporSwaggerSemAutenticacao() throws Exception {
        HttpResponse<String> resposta = enviar(HttpMethod.GET, "/v3/api-docs", null, null, null);

        assertThat(resposta.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(resposta.body()).contains("Digital Banking API");
    }

    private HttpResponse<String> enviar(HttpMethod method, String path, String body, String user, String password)
            throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15));

        if (body != null) {
            builder.header(HttpHeaders.CONTENT_TYPE, "application/json");
            builder.method(method.name(), HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8));
        } else {
            builder.method(method.name(), HttpRequest.BodyPublishers.noBody());
        }
        if (user != null) {
            builder.header(HttpHeaders.AUTHORIZATION, credencial(user, password));
        }
        return httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private String credencial(String user, String password) {
        String token = Base64.getEncoder().encodeToString(
                (user + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

}
