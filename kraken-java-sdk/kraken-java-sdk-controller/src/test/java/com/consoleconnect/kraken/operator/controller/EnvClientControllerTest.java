package com.consoleconnect.kraken.operator.controller;

import static org.hamcrest.MatcherAssert.assertThat;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class EnvClientControllerTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Autowired EnvironmentRepository environmentRepository;
  @Autowired EnvironmentClientRepository environmentClientRepository;
  public static final String RELOAD_URL =
      "/products/{productId}/envs/{envId}/clients/configuration-reload";
  public static final String HEARTBEAT_URL = "/products/{productId}/envs/{envId}/clients";
  public static final String LIST_ALL_ENV_CLIENTS_URL = "/products/{productId}/env-clients";

  @BeforeEach
  void setUp() {
    EnvironmentEntity environmentEntity = environmentRepository.findAll().get(0);

    List<EnvironmentClientEntity> all = environmentClientRepository.findAll();
    if (CollectionUtils.isNotEmpty(all)) {
      return;
    }
    EnvironmentClientEntity environmentClientEntity = new EnvironmentClientEntity();
    environmentClientEntity.setStatus("success");
    environmentClientEntity.setEnvId(environmentEntity.getId().toString());
    environmentClientEntity.setClientIp(IpUtils.getHostAddress());
    environmentClientEntity.setKind(ClientReportTypeEnum.DEPLOY.name());
    environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
    environmentClientRepository.save(environmentClientEntity);
    EnvironmentClientEntity environmentClientEntityHeat = new EnvironmentClientEntity();
    environmentClientEntityHeat.setStatus("success");
    environmentClientEntityHeat.setEnvId(environmentEntity.getId().toString());
    environmentClientEntityHeat.setClientIp(IpUtils.getHostAddress());
    environmentClientEntityHeat.setKind(ClientReportTypeEnum.HEARTBEAT.name());
    environmentClientEntityHeat.setUpdatedAt(ZonedDateTime.now());
    environmentClientRepository.save(environmentClientEntityHeat);
  }

  @Order(1)
  @Test
  void test_listClientsReload() {
    EnvironmentEntity environmentEntity = environmentRepository.findAll().get(0);

    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(RELOAD_URL).build("mef.sonata", environmentEntity.getId()))
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Order(2)
  @Test
  void test_listClients() {
    EnvironmentEntity environmentEntity = environmentRepository.findAll().get(0);
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder.path(HEARTBEAT_URL).build("mef.sonata", environmentEntity.getId()))
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("{}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }

  @Order(3)
  @Test
  void test_listClientsWithAllEnv() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(uriBuilder -> uriBuilder.path(LIST_ALL_ENV_CLIENTS_URL).build("mef.sonata"))
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              log.info("LIST_ALL_ENV_CLIENTS_URL {}", bodyStr);
              assertThat(bodyStr, Matchers.notNullValue());
            });
  }
}
