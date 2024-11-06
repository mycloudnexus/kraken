package com.consoleconnect.kraken.operator.controller.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.dto.start.ApiMappingInfoDto;
import com.consoleconnect.kraken.operator.controller.dto.start.DeploymentInfoDto;
import com.consoleconnect.kraken.operator.controller.dto.start.SellerApiServerRegistrationInfoDto;
import com.consoleconnect.kraken.operator.controller.dto.start.StartGuideInfoDto;
import com.consoleconnect.kraken.operator.controller.service.start.StartGuideService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-auth-server-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
class StartGuideControllerTest extends AbstractIntegrationTest {

  @Autowired protected ObjectMapper objectMapper;

  @MockBean private StartGuideService service;

  private final WebTestClientHelper testClientHelper;

  @Autowired
  StartGuideControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @SneakyThrows
  @Test
  void givenStartingGuideInfo_whenGettingStartGuideInfo_thenReturnsOk() {
    // given
    var kind = "kind1";
    var productId = "productId1";
    var guideInfoDto =
        new StartGuideInfoDto(
            new SellerApiServerRegistrationInfoDto(true),
            new ApiMappingInfoDto(true),
            new DeploymentInfoDto(true, true, false));
    when(service.getStartGuideInfo(productId, kind)).thenReturn(guideInfoDto);
    // when
    var path = StartGuideController.URL + "/" + productId;
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).queryParam("kind", kind).build()),
        bodyStr -> {
          // then
          var result = content(bodyStr, new TypeReference<HttpResponse<StartGuideInfoDto>>() {});
          assertThat(result.getData()).isEqualTo(guideInfoDto);
        });
  }

  @SneakyThrows
  private <T> T content(String response, TypeReference<T> typeReference) {
    return objectMapper.readValue(response, typeReference);
  }
}
