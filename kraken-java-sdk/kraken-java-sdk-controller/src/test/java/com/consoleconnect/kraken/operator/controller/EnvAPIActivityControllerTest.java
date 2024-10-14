package com.consoleconnect.kraken.operator.controller;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.google.common.collect.Maps;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EnvAPIActivityControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Autowired AppProperty appProperty;
  @Autowired ApiActivityLogRepository repository;

  public static final String PRODUCT_ID = "mef.sonata";
  public static final String DEV_ENV = "dev";
  public static final String BASE_URL =
      String.format("/products/%s/envs/%s/api-activities", PRODUCT_ID, DEV_ENV);

  @Test
  void testSearch() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(BASE_URL)
                    .queryParam(
                        "requestStartTime",
                        ZonedDateTime.now().minusDays(1).toInstant().toEpochMilli())
                    .queryParam(
                        "requestEndTime",
                        ZonedDateTime.now().plusDays(10).toInstant().toEpochMilli())
                    .queryParam("requestId", "1001")
                    .build())
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
              IpUtils.getIP(MockServerHttpRequest.get("/123").build());
            });
  }

  @Test
  void testSearchDetailEmpty() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(BASE_URL + "/{activityId}").build("11"))
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
              IpUtils.getIP(MockServerHttpRequest.get("/123").build());
            });
  }

  @Test
  void testSearchDetailExisted() {
    ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();
    apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogEntity.setPath("/123");
    apiActivityLogEntity.setUri("localhost");
    apiActivityLogEntity.setMethod("GET");
    apiActivityLogEntity.setEnv("dev");
    Map<String, String> headers = Maps.newHashMap();
    headers.put("acces_token", "2334");
    apiActivityLogEntity.setHeaders(headers);
    repository.save(apiActivityLogEntity);
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(BASE_URL + "/{activityId}")
                    .build(
                        PRODUCT_ID,
                        DEV_ENV,
                        apiActivityLogEntity.getRequestId(),
                        apiActivityLogEntity.getRequestId()))
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
              IpUtils.getIP(MockServerHttpRequest.get("/123").build());
            });
  }

  @Test
  void testGetIP() {
    MockServerHttpRequest mockServerHttpRequest = MockServerHttpRequest.get("/123").build();
    ServerHttpRequest serverHttpRequest =
        mockServerHttpRequest
            .mutate()
            .header("x-forwarded-for", "192.168.1.11,192.16.1.10")
            .build();
    String ip = IpUtils.getIP(serverHttpRequest);
    assertThat(ip, Matchers.notNullValue());
  }
}
