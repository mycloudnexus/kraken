package com.consoleconnect.kraken.operator;

import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Objects;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-no-auth")
@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@AutoConfigureObservability
class AppTest extends AbstractIntegrationTest {

  @Autowired WebTestClient webTestClient;

  @Test
  void home() {
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient
            .method(HttpMethod.GET)
            .uri(uriBuilder -> uriBuilder.path("/").build())
            .header("content-type", "application/json");
    requestBodySpec
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.PERMANENT_REDIRECT)
        .expectBody()
        .consumeWith(
            response -> {
              String location =
                  Objects.requireNonNull(response.getResponseHeaders().get("Location")).get(0);
              Assertions.assertEquals("/swagger-ui.html", location);
            });
  }

  @Test
  void getSwaggerUi() {
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient
            .method(HttpMethod.GET)
            .uri(uriBuilder -> uriBuilder.path("/swagger-ui.html").build())
            .header("content-type", "application/json");
    requestBodySpec
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.FOUND)
        .expectBody()
        .consumeWith(
            response -> {
              String location =
                  Objects.requireNonNull(response.getResponseHeaders().get("Location")).get(0);
              Assertions.assertEquals("/webjars/swagger-ui/index.html", location);
              System.out.println(location);
            });
  }

  @Test
  void getActuator() {
    webTestClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder.path("/actuator").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void getHealth() {
    webTestClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder.path("/actuator/health").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void getInfo() {
    webTestClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder.path("/actuator/info").build())
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }

  @Test
  void getPrometheus() {
    webTestClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder.path("/actuator/prometheus").build())
        .header("accept", "text/plain;version=0.0.4;charset=utf-8")
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.OK);
  }
}
