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
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:prod-it;DB_CLOSE_DELAY=-1",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=create-drop",
                "spring.sql.init.mode=never"
        }
)
@ActiveProfiles("prod")
class SwaggerProfileIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void deveDesabilitarApiDocsNoPerfilProd() throws Exception {
        HttpResponse<String> resposta = get("/v3/api-docs");

        assertThat(resposta.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deveDesabilitarSwaggerUiNoPerfilProd() throws Exception {
        HttpResponse<String> resposta = get("/swagger-ui/index.html");

        assertThat(resposta.statusCode()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + path))
                .timeout(Duration.ofSeconds(15))
                .method(HttpMethod.GET.name(), HttpRequest.BodyPublishers.noBody())
                .build();
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()
                .send(request, HttpResponse.BodyHandlers.ofString());
    }

}
