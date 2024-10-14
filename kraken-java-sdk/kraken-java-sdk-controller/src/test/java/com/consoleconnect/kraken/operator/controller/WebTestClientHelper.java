package com.consoleconnect.kraken.operator.controller;

import java.net.URI;
import java.time.Duration;
import java.util.Map;
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

  public void postAndVerify(
      Function<UriBuilder, URI> uriFunction,
      HttpStatus httpStatus,
      Object body,
      Consumer<String> verify) {
    requestAndVerify(HttpMethod.POST, uriFunction, httpStatus.value(), body, verify);
  }

  public void patchAndVerify(
      Function<UriBuilder, URI> uriFunction, Object body, Consumer<String> verify) {
    requestAndVerify(HttpMethod.PATCH, uriFunction, HttpStatus.OK.value(), body, verify);
  }

  public String requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      int statusCode,
      Object body,
      Consumer<String> verify) {
    return requestAndVerify(method, uriFunction, null, statusCode, body, verify);
  }

  public String requestAndVerify(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      Map<String, String> headers,
      int statusCode,
      Object body,
      Consumer<String> verify) {
    WebTestClient.RequestBodySpec requestBodySpec =
        webTestClient
            .mutate()
            .responseTimeout(Duration.ofSeconds(600))
            .build()
            .method(method)
            .uri(uriFunction)
            .header("content-type", "application/json");
    if (body != null) {
      requestBodySpec.bodyValue(body);
    }
    if (headers != null) {
      headers.forEach(requestBodySpec::header);
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
                    String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
                    verify.accept(bodyStr);
                  }
                });
    byte[] resp = bodyContentSpec.returnResult().getResponseBody();
    if (null == resp || resp.length == 0) {
      return "";
    }
    return new String(resp);
  }
}
