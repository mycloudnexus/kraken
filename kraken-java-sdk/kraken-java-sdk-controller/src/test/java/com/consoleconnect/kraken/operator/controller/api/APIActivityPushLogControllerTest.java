package com.consoleconnect.kraken.operator.controller.api;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.push.ApiRequestActivityPushResult;
import com.consoleconnect.kraken.operator.controller.dto.push.CreatePushApiActivityRequest;
import com.consoleconnect.kraken.operator.controller.dto.push.PushApiActivityLogHistory;
import com.consoleconnect.kraken.operator.controller.service.push.ApiActivityPushService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.request.PushLogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
class APIActivityPushLogControllerTest extends AbstractIntegrationTest {

  @MockBean private ApiActivityPushService service;
  @Autowired private ObjectMapper objectMapper;
  private final WebTestClientHelper testClientHelper;

  @Autowired
  APIActivityPushLogControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Test
  void givenApiActivityLogs_whenCreatingApiLogsPushRequest_thenReturnsOk() {
    // given
    var envId = UUID.randomUUID();
    var now = ZonedDateTime.now(ZoneOffset.UTC);
    var request = new CreatePushApiActivityRequest(now.minusDays(10), now, envId.toString());

    var pushResult = new ApiRequestActivityPushResult(UUID.randomUUID());
    when(service.createPushApiActivityLogInfo(request, "anonymous")).thenReturn(pushResult);
    // when
    var path = "/push-api-activity-log";
    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        request,
        bodyStr -> {
          // then
          var result =
              content(bodyStr, new TypeReference<HttpResponse<ApiRequestActivityPushResult>>() {});
          assertThat(result.getData()).isEqualTo(pushResult);
        });
  }

  @Test
  void givenApiActivityLogs_whenSearchPushHistory_thenReturnsOk() {
    // given
    var pageRequest =
        getSearchPageRequest(
            PagingHelper.DEFAULT_PAGE, PagingHelper.DEFAULT_SIZE, Sort.Direction.DESC, "createdAt");
    var historyList = List.of(new PushApiActivityLogHistory());
    when(service.searchHistory(PushLogSearchRequest.builder().build(), pageRequest))
        .thenReturn(PagingHelper.toPage(historyList, 0, 1));
    // when
    var path = "/push-api-activity-log/history";
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          // then
          var result =
              content(
                  bodyStr, new TypeReference<HttpResponse<Paging<PushApiActivityLogHistory>>>() {});
          assertThat(result.getData().getData()).isEqualTo(historyList);
        });
  }

  @SneakyThrows
  private <T> T content(String response, TypeReference<T> typeReference) {
    return objectMapper.readValue(response, typeReference);
  }
}