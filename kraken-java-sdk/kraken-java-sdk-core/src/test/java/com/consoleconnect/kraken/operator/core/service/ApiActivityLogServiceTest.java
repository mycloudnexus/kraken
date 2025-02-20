package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.PRODUCT_BUYER;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.BuyerOnboardFacets;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.request.LogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class ApiActivityLogServiceTest extends AbstractIntegrationTest {

  @Autowired private ApiActivityLogRepository apiActivityLogRepository;
  @Autowired private UnifiedAssetRepository unifiedAssetRepository;
  @Autowired private UnifiedAssetService unifiedAssetService;

  @SpyBean private ApiActivityLogService apiActivityLogService;

  private static final String STAGE_ENV = "stage";

  @Test
  @Order(1)
  void givenExistedActivityId_whenSearchDetail_thenReturnOK() {
    String buyerId = "test-log" + System.currentTimeMillis();
    createBuyer(buyerId, STAGE_ENV, "test-company-name-" + System.currentTimeMillis());
    ApiActivityLogEntity entity = createApiActivityLog(buyerId, STAGE_ENV);
    Optional<ComposedHttpRequest> optional = apiActivityLogService.getDetail(entity.getRequestId());
    Assertions.assertTrue(optional.isPresent());
    Assertions.assertNotNull(optional.get());
    log.info("result:{}", JsonToolkit.toJson(optional.get()));
  }

  @Test
  @Order(2)
  void givenTimeRange_whenSearchActivities_thenReturnOK() {
    String buyerId = "test-log" + System.currentTimeMillis();
    createBuyer(buyerId, STAGE_ENV, "test-company-name-" + System.currentTimeMillis());
    ApiActivityLogEntity entity = createApiActivityLog(buyerId, STAGE_ENV);
    LogSearchRequest logSearchRequest =
        LogSearchRequest.builder()
            .env(STAGE_ENV)
            .requestId(entity.getRequestId())
            .statusCode("200, 201")
            .method("GET, POST")
            .queryStart(ZonedDateTime.now().minusDays(1))
            .queryEnd(ZonedDateTime.now().plusDays(10))
            .statusCode(200)
            .productType("access.eline")
            .build();
    Paging<ApiActivityLog> pages =
        apiActivityLogService.search(logSearchRequest, PageRequest.of(0, 10));
    log.info("pages:{}", JsonToolkit.toJson(pages));
    Assertions.assertNotNull(pages.getData());
    Assertions.assertFalse(pages.getData().isEmpty());
  }

  public ApiActivityLogEntity createApiActivityLog(String buyerId, String envId) {
    ApiActivityLogEntity apiActivityLogEntity = new ApiActivityLogEntity();
    apiActivityLogEntity.setRequestId(UUID.randomUUID().toString());
    apiActivityLogEntity.setPath("/123");
    apiActivityLogEntity.setUri("localhost");
    apiActivityLogEntity.setMethod("GET");
    apiActivityLogEntity.setEnv(envId);
    Map<String, String> headers = Maps.newHashMap();
    headers.put("acces_token", "2334");
    apiActivityLogEntity.setHeaders(headers);
    apiActivityLogEntity.setBuyer(buyerId);
    apiActivityLogEntity.setCallSeq(0);
    apiActivityLogEntity.setHttpStatusCode(200);
    apiActivityLogEntity.setTriggeredAt(ZonedDateTime.now());
    apiActivityLogEntity.setProductType("access.eline");
    apiActivityLogEntity.setHttpStatusCode(200);
    apiActivityLogEntity = apiActivityLogRepository.save(apiActivityLogEntity);
    return apiActivityLogEntity;
  }

  private IngestionDataResult createBuyer(String buyerId, String envId, String companyName) {
    String key = "mef.sonata.buyer" + System.currentTimeMillis();
    UnifiedAsset unifiedAsset = UnifiedAsset.of(PRODUCT_BUYER.getKind(), key, "Buyer");
    unifiedAsset.getMetadata().setDescription("Onboard buyer information");
    unifiedAsset.getMetadata().getLabels().put(LABEL_ENV_ID, envId);
    unifiedAsset.getMetadata().getLabels().put(LABEL_BUYER_ID, buyerId);
    unifiedAsset
        .getMetadata()
        .getLabels()
        .put(LABEL_ISSUE_AT, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    BuyerOnboardFacets facets = new BuyerOnboardFacets();
    BuyerOnboardFacets.BuyerInfo buyerInfo = new BuyerOnboardFacets.BuyerInfo();
    buyerInfo.setBuyerId(buyerId);
    buyerInfo.setEnvId(envId);
    buyerInfo.setCompanyName(companyName);
    facets.setBuyerInfo(buyerInfo);

    unifiedAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), "unit-test");
    return unifiedAssetService.syncAsset(
        "product.mef.sonata.api", unifiedAsset, syncMetadata, true);
  }
}
