package com.consoleconnect.kraken.operator.controller.v2;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.APIServerCreator;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.List;
import lombok.Getter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = TestApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ComponentAPIServerControllerTest extends AbstractIntegrationTest implements APIServerCreator {

  @Autowired WebTestClient webTestClient;
  @Getter private final WebTestClientHelper testClientHelper;

  @Autowired
  ComponentAPIServerControllerTest(WebTestClient webTestClient) {
    testClientHelper = new WebTestClientHelper(webTestClient);
  }

  @Order(1)
  @Test
  void testCreateAPIServer() {
    Assertions.assertDoesNotThrow(() -> createAPIServer(PRODUCT_ID, COMPONENT_ID));
  }

  @Order(2)
  @Test
  void testSearchAPIServers() {
    String path =
        String.format(
            "%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);
    testClientHelper.getAndVerify(
        (uriBuilder -> uriBuilder.path(path).build()),
        bodyStr -> {
          assertThat(bodyStr, hasJsonPath("$.data.data", hasSize(greaterThanOrEqualTo(1))));
        });
  }

  @Order(3)
  @Test
  void givenLiteSearchWithTrue_whenQueryServerAPIList_thenReturnOK() {
    String path =
        String.format(
            "%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);
    List<UnifiedAssetDto> assetDtoList = queryAPIServerList(path, false, false);
    String bodyStr = JsonToolkit.toJson(assetDtoList);
    assertThat(bodyStr, hasJsonPath("$", hasSize(1)));
    assertThat(bodyStr, hasJsonPath("$[0].syncMetadata", notNullValue()));

    assetDtoList = queryAPIServerList(path, true, true);
    bodyStr = JsonToolkit.toJson(assetDtoList);
    System.out.println(bodyStr);
    assertThat(bodyStr, hasJsonPath("$", hasSize(1)));
    assertThat(bodyStr, hasNoJsonPath("$[0].syncMetadata"));
    assertThat(bodyStr, hasJsonPath("$[0].facets.environments", notNullValue()));
  }

  @Order(4)
  @Test
  void givenAPIServer_whenDeleting_thenResponseOK() {
    String path =
        String.format(
            "%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID);

    List<UnifiedAssetDto> assetDtoList = queryAPIServerList(path);

    Assertions.assertFalse(assetDtoList.isEmpty());
    Assertions.assertNotNull(assetDtoList.get(0).getMetadata());
    Assertions.assertNotNull(assetDtoList.get(0).getMetadata().getKey());
    deleteAPIServer(assetDtoList.get(0).getMetadata().getKey());

    createAPIServer(PRODUCT_ID, COMPONENT_ID);
  }

  private void deleteAPIServer(String specKey) {
    String deletePath =
        String.format("%s/%s/components/%s/api-servers", PRODUCT_BASE_PATH, PRODUCT_ID, specKey);
    testClientHelper.requestAndVerify(
        HttpMethod.DELETE,
        uriBuilder -> uriBuilder.path(deletePath).build(),
        HttpStatus.OK.value(),
        null,
        Assertions::assertNotNull);
  }

  @Order(5)
  @Test
  void givenAnExistedAPIServerName_whenCheckIt_thenShouldThrowException() {
    String name = "Geographic Address Management";
    String path =
        String.format(
            "%s/%s/components/%s/api-servers/%s",
            PRODUCT_BASE_PATH, PRODUCT_ID, COMPONENT_ID, name);

    getTestClientHelper()
        .getAndVerify(
            (uriBuilder -> uriBuilder.path(path).build()),
            bodyStr -> {
              assertThat(bodyStr, hasJsonPath("$.code", equalTo(400)));
              assertThat(bodyStr, hasJsonPath("$.data", equalTo(false)));
            });
  }
}
