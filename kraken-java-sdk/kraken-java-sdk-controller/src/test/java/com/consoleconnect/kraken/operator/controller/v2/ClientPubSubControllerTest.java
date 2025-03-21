package com.consoleconnect.kraken.operator.controller.v2;

import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_SUB_VERSION_NAME;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_VERSION_NAME;
import static org.mockito.ArgumentMatchers.*;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.APITokenCreator;
import com.consoleconnect.kraken.operator.controller.WebTestClientHelper;
import com.consoleconnect.kraken.operator.controller.model.ComponentTag;
import com.consoleconnect.kraken.operator.controller.model.DeploymentFacet;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
import com.consoleconnect.kraken.operator.controller.service.EvnClientMapperInfoService;
import com.consoleconnect.kraken.operator.core.client.*;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.entity.AssetFacetEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStatusEnum;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.request.LogSearchRequest;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test-hs512")
@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientPubSubControllerTest extends AbstractIntegrationTest implements APITokenCreator {
  @Getter @Autowired APITokenService apiTokenService;
  @Autowired ApiActivityLogService apiActivityLogService;
  @SpyBean ApiActivityLogRepository apiActivityLogRepository;
  @SpyBean UnifiedAssetRepository unifiedAssetRepository;
  @SpyBean EvnClientMapperInfoService evnClientMapperInfoService;

  public static final String PRODUCT_ID = "mef.sonata";
  public static final String CLIENT_EVENT_ENDPOINT = "/client/events";

  public static String accessToken;

  private final WebTestClientHelper webTestClientHelper;

  @Autowired
  public ClientPubSubControllerTest(WebTestClient webTestClient) {
    this.webTestClientHelper = new WebTestClientHelper(webTestClient);
  }

  @BeforeEach
  public void setup() {
    if (accessToken == null) {
      accessToken = "Bearer " + createToken(TestApplication.envId, PRODUCT_ID).getToken();
    }
  }

  @Test
  @Order(1)
  void givenAPILogEvent_whenOnEvent_thenEventPersisted() {
    // given
    ApiActivityLog requestEntity = new ApiActivityLog();
    requestEntity.setUri("localhost");
    requestEntity.setMethod("get");
    requestEntity.setPath("/123");
    requestEntity.setRequestId(UUID.randomUUID().toString());
    requestEntity.setCallSeq(0);
    List<ApiActivityLog> logs = List.of(requestEntity);

    ClientEvent event =
        ClientEvent.of(
            UUID.randomUUID().toString(),
            ClientEventTypeEnum.CLIENT_API_AUDIT_LOG,
            JsonToolkit.toJson(logs));

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", accessToken);

    // when
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        event,
        Assertions::assertNotNull);

    // then
    LogSearchRequest logSearchRequest =
        LogSearchRequest.builder()
            .requestId(requestEntity.getRequestId())
            .env(TestApplication.envId)
            .build();
    List<ApiActivityLog> persistedLogs =
        apiActivityLogService.search(logSearchRequest, PageRequest.of(0, 1)).getData();
    Assertions.assertNotNull(persistedLogs);
    Assertions.assertEquals(1, persistedLogs.size());
    ApiActivityLog persistedLog = persistedLogs.get(0);
    Assertions.assertEquals(requestEntity.getRequestId(), persistedLog.getRequestId());
    Assertions.assertEquals(requestEntity.getUri(), persistedLog.getUri());
    Assertions.assertEquals(requestEntity.getMethod(), persistedLog.getMethod());
    Assertions.assertEquals(requestEntity.getPath(), persistedLog.getPath());
    Assertions.assertEquals(requestEntity.getCallSeq(), persistedLog.getCallSeq());
  }

  @Test
  @Order(2)
  void givenClientDeploymentEvent_whenUploadEvent_thenResponseOk() {

    UnifiedAssetEntity deployment = new UnifiedAssetEntity();
    deployment.setKind(AssetKindEnum.PRODUCT_DEPLOYMENT.getKind());
    deployment.setApiVersion("1");
    deployment.setKey("test");
    deployment.setStatus(DeployStatusEnum.FAILED.name());

    Set<AssetFacetEntity> facets = new HashSet<>();
    List<ComponentTag> componentTags = new ArrayList<>();
    ComponentTag tag = new ComponentTag();
    tag.setTagId("v1");
    componentTags.add(tag);
    DeploymentFacet deploymentFacet = new DeploymentFacet();
    deploymentFacet.setComponentTags(componentTags);
    AssetFacetEntity facetEntity = new AssetFacetEntity();
    facetEntity.setKey("componentTags");
    facetEntity.setPayload(componentTags);
    facetEntity.setAsset(deployment);
    facets.add(facetEntity);
    deployment.setFacets(facets);
    deployment = unifiedAssetRepository.save(deployment);

    ClientEvent event = new ClientEvent();
    event.setClientId(UUID.randomUUID().toString());
    event.setEventType(ClientEventTypeEnum.CLIENT_DEPLOYMENT);

    ClientInstanceDeployment clientInstanceDeployment = new ClientInstanceDeployment();
    clientInstanceDeployment.setInstanceId(event.getClientId());
    clientInstanceDeployment.setProductReleaseId(deployment.getId().toString());
    clientInstanceDeployment.setStatus(DeployStatusEnum.SUCCESS.name());
    event.setEventPayload(JsonToolkit.toJson(clientInstanceDeployment));

    UnifiedAssetEntity tagAsset = new UnifiedAssetEntity();
    tagAsset.setKind(AssetKindEnum.COMPONENT_API_TAG.getKind());
    tagAsset.setLabels(
        Map.of(
            LABEL_VERSION_NAME, "1",
            LABEL_SUB_VERSION_NAME, "1"));
    Mockito.doReturn(Optional.of(tagAsset))
        .when(unifiedAssetRepository)
        .findOneByKey(Mockito.any());

    Mockito.doNothing().when(evnClientMapperInfoService).handleSingleMapperInfo(Mockito.any());

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", accessToken);
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        event,
        Assertions::assertNotNull);

    deployment.setStatus(DeployStatusEnum.FAILED.name());
    DeployComponentError error =
        DeployComponentError.builder()
            .severity(ErrorSeverityEnum.FATAL)
            .reason("Failed to deploy workflow")
            .build();
    clientInstanceDeployment.setStatus(DeployStatusEnum.FAILED.name());
    clientInstanceDeployment.setErrors(List.of(error));
    event.setEventPayload(JsonToolkit.toJson(clientInstanceDeployment));
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        event,
        Assertions::assertNotNull);

    event.setEventPayload(null);
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        event,
        Assertions::assertNotNull);

    unifiedAssetRepository.delete(unifiedAssetRepository.findById(deployment.getId()).get());
  }

  @Test
  @Order(2)
  void givenClientHeartbeatEvent_whenUploadEvent_thenResponseOk() {

    ClientEvent event = new ClientEvent();
    event.setClientId(UUID.randomUUID().toString());
    event.setEventType(ClientEventTypeEnum.CLIENT_HEARTBEAT);

    ClientInstanceHeartbeat instance1 = new ClientInstanceHeartbeat();
    instance1.setInstanceId("1");
    instance1.setUpdatedAt(DateTime.nowInUTC());

    ClientInstanceHeartbeat instance2 = new ClientInstanceHeartbeat();
    instance2.setInstanceId("2");
    instance2.setUpdatedAt(DateTime.nowInUTC());
    instance2.setAppVersion("1.1.0");
    event.setEventPayload(JsonToolkit.toJson(List.of(instance1, instance2)));

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", accessToken);
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        event,
        Assertions::assertNotNull);
  }

  @Test
  @Order(2)
  void givenClientServerAPIEvent_whenUploadEvent_thenResponseOK() {
    ClientEvent event = new ClientEvent();
    event.setClientId(UUID.randomUUID().toString());
    event.setEventType(ClientEventTypeEnum.CLIENT_SERVER_API);

    ServerAPIDto serverAPIDto = new ServerAPIDto();
    serverAPIDto.setServerKey("mef.sonata.api-target-spec.con1718940696857");
    serverAPIDto.setPath("/api/pricing/calculate");
    serverAPIDto.setMethod("post");
    serverAPIDto.setMapperKey("mef.sonata.api-target-mapper.quote.eline.add.sync");
    List<ServerAPIDto> serverAPIDtoList = List.of(serverAPIDto);
    event.setEventPayload(JsonToolkit.toJson(serverAPIDtoList));

    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", accessToken);
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        event,
        Assertions::assertNotNull);
  }

  @Test
  void givenClientWorkflowStatusEvent_whenTriggerEvent_thenResponseOK() {
    ClientWorkflowTerminate terminate = new ClientWorkflowTerminate();
    terminate.setErrorMessage("unexpected error");
    terminate.setStatus(WorkflowStatusEnum.FAILED.name());
    terminate.setRequestId(UUID.randomUUID().toString());
    ClientEvent clientEvent =
        ClientEvent.of(
            UUID.randomUUID().toString(),
            ClientEventTypeEnum.CLIENT_TERMINATE_WORKFLOW,
            List.of(terminate));
    Mockito.doReturn(Optional.of(new ApiActivityLogEntity()))
        .when(apiActivityLogRepository)
        .findByRequestIdAndCallSeq(anyString(), anyInt());
    Mockito.doReturn(new ApiActivityLogEntity()).when(apiActivityLogRepository).save(any());
    Map<String, String> headers = new HashMap<>();
    headers.put("Authorization", accessToken);
    webTestClientHelper.requestAndVerify(
        HttpMethod.POST,
        uriBuilder -> uriBuilder.path(CLIENT_EVENT_ENDPOINT).build(),
        headers,
        HttpStatus.OK.value(),
        clientEvent,
        Assertions::assertNotNull);
  }
}
