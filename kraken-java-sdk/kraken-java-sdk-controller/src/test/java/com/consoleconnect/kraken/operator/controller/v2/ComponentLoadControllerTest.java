package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class ComponentLoadControllerTest extends AbstractIntegrationTest {
  private final WebTestClientHelper testClientHelper;
  @SpyBean UnifiedAssetService unifiedAssetService;

  @Autowired
  public ComponentLoadControllerTest(WebTestClient webTestClient) {
    this.testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Test
  void givenKey_thenSuccess() {
    UnifiedAssetDto unifiedAsset = new UnifiedAssetDto();
    Metadata metadata = new Metadata();
    metadata.setName("mock seller API");
    metadata.setKey("server-key-001");
    unifiedAsset.setMetadata(metadata);
    Mockito.doReturn(List.of(unifiedAsset)).when(unifiedAssetService).findByKind(anyString());

    testClientHelper.getAndVerify(
        (uriBuilder ->
            uriBuilder
                .path("/v2/components/mappers/load/mef.sonata.api-target-mapper.order.uni.add")
                .build()),
        bodyStrVerified -> {
          log.info("testVerified result {}", bodyStrVerified);
          assertThat(bodyStrVerified, hasJsonPath("$.data.id", notNullValue()));
        });
  }
}
