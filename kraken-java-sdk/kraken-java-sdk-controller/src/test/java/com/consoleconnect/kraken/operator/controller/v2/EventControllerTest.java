package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.controller.repo.EventRepository;
import com.consoleconnect.kraken.operator.core.dto.UpdateStatusDto;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class EventControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;

  @SpyBean private EventRepository eventRepository;

  @Autowired
  public EventControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Test
  @Order(1)
  void givenDataPath_thenOK() {
    MgmtEventEntity entity = new MgmtEventEntity();
    entity.setStatus(EventStatusType.ACK);
    entity.setEventType(MgmtEventType.RESET);
    entity.setResourceId("mef.sonata.api-target-mapper.order.uni.add");
    entity.setId(UUID.randomUUID());
    Mockito.doReturn(Optional.of(entity)).when(eventRepository).findById(ArgumentMatchers.any());
    testClientHelper.requestAndVerify(
        HttpMethod.POST,
        (uriBuilder ->
            uriBuilder
                .path("/v2/components/mappers/reset/mef.sonata.api-target-mapper.order.uni.add")
                .build()),
        200,
        null,
        bodyStrVerified -> {
          log.info("testVerified result {}", bodyStrVerified);
          assertThat(bodyStrVerified, hasJsonPath("$.data", notNullValue()));
        });
  }

  @Test
  @Order(2)
  void givenEventCreated_thenReturnRecord() {
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path("/v2/callback/event").queryParam("status", "ACK").build()),
        bodyStrVerified -> {
          log.info("testVerified result {}", bodyStrVerified);
          assertThat(bodyStrVerified, hasJsonPath("$.data", notNullValue()));
          assertThat(bodyStrVerified, hasJsonPath("$.data.total", equalTo(1)));
        });
  }

  @Test
  @Order(3)
  void givenEventId_thenOK() {
    List<MgmtEventEntity> list = eventRepository.findAll();
    MgmtEventEntity mgmtEventEntity = list.get(0);
    UpdateStatusDto dto = new UpdateStatusDto();
    dto.setStatus(EventStatusType.FAILED.name());
    dto.setIds(List.of(mgmtEventEntity.getId().toString()));
    testClientHelper.patchAndVerify(
        (uriBuilder -> uriBuilder.path("/v2/callback/event").build()),
        dto,
        bodyStrVerified -> {
          log.info("testVerified result {}", bodyStrVerified);
          Optional<MgmtEventEntity> mgmtEvent = eventRepository.findById(mgmtEventEntity.getId());
          assertTrue(mgmtEvent.isPresent());
          assertThat(mgmtEvent.get().getStatus().name(), equalTo("FAILED"));
        });
  }
}
