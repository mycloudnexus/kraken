package com.consoleconnect.kraken.operator.gateway;

import static com.consoleconnect.kraken.operator.gateway.CustomConfig.X_KRAKEN_KEY_TOKEN;
import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.X_KRAKEN_AUTH_KEY;
import static com.consoleconnect.kraken.operator.gateway.runner.LoadTargetAPIConfigActionRunner.encodeUrlParam;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.model.WorkflowResponse;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.netflix.conductor.common.metadata.workflow.WorkflowDef;
import com.netflix.conductor.common.run.Workflow;
import io.orkes.conductor.client.http.OrkesMetadataClient;
import io.orkes.conductor.client.http.OrkesWorkflowClient;
import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GatewayTest extends AbstractIntegrationTest {
  @Autowired WebTestClient webTestClient;
  @Autowired ApiActivityLogRepository repository;

  @MockBean OrkesWorkflowClient workflowClient;
  @MockBean OrkesMetadataClient metaDataClient;

  @Test
  @Order(1)
  void testMefOrderAddUni() throws IOException {
    String bodyJsonString = readFileToString("mef/order/orderPortBody.json");
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .post()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/mefApi/sonata/productOrderingManagement/v10/productOrder")
                    .queryParam("buyerId", "cc-company")
                    .build())
        .bodyValue(JsonToolkit.fromJson(bodyJsonString, Object.class))
        .header("portal-token", "123")
        .header(X_KRAKEN_AUTH_KEY, X_KRAKEN_KEY_TOKEN)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
            });
  }

  @Order(2)
  @Test
  void testListLogs() {
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/env/{env}/logs")
                    .queryParam(
                        "requestStartTime",
                        ZonedDateTime.now().minusDays(1).toInstant().toEpochMilli())
                    .build("dev"))
        .header(X_KRAKEN_AUTH_KEY, X_KRAKEN_KEY_TOKEN)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
            });
  }

  @Order(3)
  @Test
  void testGetLogDetail() {
    List<ApiActivityLogEntity> all = repository.findAll();
    if (CollectionUtils.isNotEmpty(all)) {
      String requestId = all.get(0).getRequestId();
      webTestClient
          .mutate()
          .responseTimeout(Duration.ofSeconds(600))
          .build()
          .get()
          .uri(
              uriBuilder ->
                  uriBuilder
                      .path("/env/{env}/logs/{requestId}")
                      .queryParam(
                          "requestStartTime",
                          ZonedDateTime.now().minusDays(1).toInstant().toEpochMilli())
                      .build("dev", requestId))
          .exchange()
          .expectBody()
          .consumeWith(
              response -> {
                String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
                System.out.println(bodyStr);
              });
    }
  }

  @Test
  void testMefOrderAddEAccessLine() throws IOException {
    String bodyJsonString = readFileToString("mef/order/orderConnectionBody.json");
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .post()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/mefApi/sonata/productOrderingManagement/v10/productOrder")
                    .queryParam("buyerId", "cc-company")
                    .build())
        .bodyValue(JsonToolkit.fromJson(bodyJsonString, Object.class))
        .header("portal-token", "123")
        .header(X_KRAKEN_AUTH_KEY, X_KRAKEN_KEY_TOKEN)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
            });
  }

  @Order(4)
  @Test
  void testMefOrderReadById() {
    webTestClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/mefApi/sonata/productOrderingManagement/v10/productOrder/1234")
                    .build())
        .header("portal-token", "123")
        .header(X_KRAKEN_AUTH_KEY, X_KRAKEN_KEY_TOKEN)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
            });
  }

  @Test
  void testMockResponse() {
    webTestClient
        .get()
        .uri(uriBuilder -> uriBuilder.path("/actions/mock-response").build())
        .header("access-token", "123")
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              assertThat(response.getStatus().value(), is(201));
              assertThat(response.getResponseHeaders().containsKey("access-token"), is(false));
              assertThat(response.getResponseHeaders().containsKey("x-kraken-tenant-id"), is(true));
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              assertThat(bodyStr, hasJsonPath("$.data", is("mocked")));
            });
  }

  @SneakyThrows
  @Test
  @Order(5)
  @Sql(
      statements = {
        "INSERT INTO kraken_asset (id, created_at, created_by, deleted_at, deleted_by, updated_at, updated_by, api_version, description, key, kind, labels, logo, metadata, name, parent_id, sync_metadata, tags, version, mapper_key, status) VALUES ('9fcfe4ae-2fa4-4954-b4d4-4edac32fa7df', '2024-06-26 02:25:45.387279 +00:00', null, null, null, '2024-06-26 02:41:59.980069 +00:00', null, 'v1', 'api server url info', 'kraken.component.api-server', 'kraken.component.api-server', null, null, '{}', null, '729cf697-9357-4e6e-ab48-558700cbe122', '{\"fullPath\": \"raw:{\\\"kind\\\":\\\"kraken.component.api-server\\\",\\\"apiVersion\\\":\\\"v1\\\",\\\"metadata\\\":{\\\"version\\\":3,\\\"key\\\":\\\"kraken.component.api-server\\\",\\\"description\\\":\\\"api server url info\\\"},\\\"facets\\\":{\\\"urls\\\":[{\\\"apiServerKey\\\":\\\"mef.sonata.api-target-spec.cc01719206860796\\\",\\\"url\\\":\\\"https://api.test.consoleconnect.com\\\"},{\\\"apiServerKey\\\":\\\"mef.sonata.api-target-spec.t021718950690882\\\",\\\"url\\\":\\\"https://api.test.consoleconnect.com\\\"}]},\\\"syncMetadata\\\":{\\\"syncedAt\\\":\\\"2024-06-26T02:41:59.963699267Z\\\"}}\", \"syncedAt\": \"2024-06-26T02:41:59.970482149Z\", \"syncedSha\": \"66B9426D4D44C315CF21473304E831AC\"}', null, 3, null, null);\n",
        "INSERT INTO kraken_asset_facet (id, created_at, created_by, deleted_at, deleted_by, updated_at, updated_by, key, payload, asset_id) VALUES ('57f3ed5a-3e94-48bc-92a3-03ac91b7d9ba', '2024-06-26 02:41:59.987778 +00:00', null, null, null, null, null, 'urls', '[{\"url\": \"https://api.test.consoleconnect.com\", \"apiServerKey\": \"mef.sonata.api-target-spec.cc01719206860796\"}, {\"url\": \"https://api.test.consoleconnect.com\", \"apiServerKey\": \"mef.sonata.api-target-spec.t021718950690882\"}]', '9fcfe4ae-2fa4-4954-b4d4-4edac32fa7df');\n"
      })
  void testMockAddressValidate() {
    String bodyJsonString = readFileToString("mef/order/cc_address_validate_req.json");
    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .post()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(
                        "/mefApi/sonata/geographicAddressManagement/v7/geographicAddressValidation")
                    .queryParam("buyerId", "cc-company")
                    .queryParam("page", 0)
                    .queryParam("pageSize", 24)
                    .queryParam("criteria", "%7B%7D")
                    .build())
        .bodyValue(JsonToolkit.fromJson(bodyJsonString, Object.class))
        .header("portal-token", "123")
        .header("Authorization", "Bearer 122222")
        .header(X_KRAKEN_AUTH_KEY, X_KRAKEN_KEY_TOKEN)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
            });
  }

  @Order(7)
  @SneakyThrows
  @Test
  void givenOrderPayload_whenDeleteOrder_thenSuccess() {
    WorkflowDef def = new WorkflowDef();
    def.setVersion(1);
    String id = "workflow_id";
    Workflow workflow = new Workflow();
    workflow.setWorkflowDefinition(def);
    WorkflowResponse workflowResponse = new WorkflowResponse();
    WorkflowResponse.ItemResponse itemResponse = new WorkflowResponse.ItemResponse();
    itemResponse.setId(id);
    itemResponse.setResponse(new HashMap<>());
    workflowResponse.setResult(Map.of(id, itemResponse));
    workflow.setOutput(JsonToolkit.fromJson(JsonToolkit.toJson(workflowResponse), Map.class));
    workflow.setStatus(Workflow.WorkflowStatus.COMPLETED);
    Mockito.doReturn(def).when(metaDataClient).getWorkflowDef(anyString(), isNull());
    Mockito.doReturn(id).when(workflowClient).startWorkflow(any());
    Mockito.doReturn(workflow).when(workflowClient).getWorkflow(anyString(), anyBoolean());

    webTestClient
        .mutate()
        .responseTimeout(Duration.ofSeconds(600))
        .build()
        .post()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path("/mefApi/sonata/productOrderingManagement/v10/productOrder")
                    .queryParam("buyerId", "cc-company")
                    .build())
        .bodyValue(
            JsonToolkit.fromJson(
                readFileToString("mockData/delete.order.eline.json"), Object.class))
        .header(X_KRAKEN_AUTH_KEY, X_KRAKEN_KEY_TOKEN)
        .exchange()
        .expectBody()
        .consumeWith(
            response -> {
              String bodyStr = new String(Objects.requireNonNull(response.getResponseBody()));
              System.out.println(bodyStr);
            });
  }

  @Test
  void givenEmptyPath_whenEncode_thenReturnNull() {
    Assertions.assertEquals(StringUtils.EMPTY, encodeUrlParam(""));
  }
}
