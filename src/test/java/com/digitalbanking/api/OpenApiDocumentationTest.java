package com.digitalbanking.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:opendocs;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.sql.init.mode=never"
        }
)
class OpenApiDocumentationTest {

    @LocalServerPort
    private int port;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Test
    void deveExporApiDocsComMetadadosECaminhos() throws Exception {
        HttpResponse<String> resposta = get("/v3/api-docs");

        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.headers().firstValue("content-type").orElse(""))
                .contains("application/json");

        String corpo = resposta.body();
        assertThat(corpo).contains("Digital Banking API");
        assertThat(corpo).contains("\"version\":\"1.0.0\"");
        assertThat(corpo).contains("/clientes");
        assertThat(corpo).contains("/contas");
        assertThat(corpo).contains("/transferencias");
        assertThat(corpo).contains("/contas/{id}/depositos");
        assertThat(corpo).contains("/contas/{id}/saques");
        assertThat(corpo).contains("/contas/{id}/extrato");
    }

    @Test
    void deveServirSwaggerUi() throws Exception {
        HttpResponse<String> resposta = get("/swagger-ui/index.html");

        assertThat(resposta.statusCode()).isEqualTo(200);
        assertThat(resposta.body().toLowerCase()).contains("swagger");
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

}
