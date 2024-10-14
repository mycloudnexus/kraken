package com.consoleconnect.kraken.operator.core.helper;

import java.net.URI;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@AllArgsConstructor
public class WebTestClientHelper {

  private WebTestClient webTestClient;

  public void getAndVerify(Function<UriBuilder, URI> uriFunction, Consumer<String> verify) {
    requestAndVerify(HttpMethod.GET, uriFunction, HttpStatus.OK.value(), null, verify);
  }

  public void getAndVerify(
      Function<UriBuilder, URI> uriFunction, HttpStatus httpStatus, Consumer<String> verify) {
    requestAndVerify(HttpMethod.GET, uriFunction, httpStatus.value(), null, verify);
  }

  public void postAndVerify(Function<UriBuilder, URI> uriFunction, Consumer<String> verify) {
    requestAndVerify(HttpMethod.POST, uriFunction, HttpStatus.OK.value(), null, verify);
  }

  public void postAndVerify(
      Function<UriBuilder, URI> uriFunction, Object body, Consumer<String> verify) {
    requestAndVerify(HttpMethod.POST, uriFunction, HttpStatus.OK.value(), body, verify);
  }

  public void requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      int statusCode,
      Object body,
      Consumer<String> verify) {
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient.method(method).uri(uriFunction).header("content-type", "application/json");
    if (body != null) {
      requestBodySpec.bodyValue(body);
    }
    requestBodySpec
        .exchange()
        .expectStatus()
        .isEqualTo(statusCode)
        .expectBody()
        .consumeWith(
            response -> {
              if (verify != null) {
                String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
                verify.accept(bodyStr);
              }
            });
  }
}
