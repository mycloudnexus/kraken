package com.consoleconnect.kraken.operator.auth.helper;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriBuilder;

@AllArgsConstructor
public class WebTestClientHelper {

  private WebTestClient webTestClient;

  public void requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      int statusCode,
      Consumer<String> verify) {
    this.requestAndVerify(method, uriFunction, null, null, statusCode, verify);
  }

  public Optional<String> requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      Map<String, String> headers,
      Object body,
      int statusCode,
      Consumer<String> verify) {
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient.method(method).uri(uriFunction).header("content-type", "application/json");
    if (headers != null) {
      headers.forEach(requestBodySpec::header);
    }
    if (body != null) {
      requestBodySpec.bodyValue(body);
    }
    WebTestClient.BodyContentSpec bodyContentSpec =
        requestBodySpec
            .exchange()
            .expectStatus()
            .isEqualTo(statusCode)
            .expectBody()
            .consumeWith(
                response -> {
                  if (verify != null) {
                    if (response.getResponseBody() == null) {
                      verify.accept(null);
                      return;
                    } else {
                      String bodyStr =
                          new String(Objects.requireNonNull(response.getResponseBody()));
                      verify.accept(bodyStr);
                    }
                  }
                });

    byte[] resp = bodyContentSpec.returnResult().getResponseBody();
    if (null == resp || resp.length == 0) {
      return Optional.empty();
    }
    return Optional.of(new String(resp));
  }
}
