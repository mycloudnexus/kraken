package com.consoleconnect.kraken.operator.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.RecordedRequest;

public class RequestVerifier {

  private final ObjectMapper objectMapper = new ObjectMapper();

  private final RecordedRequest request;

  RequestVerifier(RecordedRequest request) {
    assertThat("Request is null", request, is(notNullValue()));
    this.request = request;
  }

  public RequestVerifier expectPath(String path) {
    assertThat(request.getPath(), is(path));
    return this;
  }

  public RequestVerifier expectHeader(String name, String value) {
    assertThat(request.getHeader(name), is(value));
    return this;
  }

  public RequestVerifier expectMethod(String method) {
    assertThat(request.getMethod(), is(method));
    return this;
  }

  public <T> RequestVerifier expectBody(T body, Class<T> bodyClass) throws JsonProcessingException {
    String actualBodyContent = request.getBody().readUtf8();
    T actualBody = objectMapper.readValue(actualBodyContent, bodyClass);
    assertThat(actualBody, is(body));
    return this;
  }
}
