package com.consoleconnect.kraken.operator.sync.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PushRunningMapperInfoServiceTest extends AbstractIntegrationTest {

  @SpyBean private PushRunningMapperInfoService pushServerAPIService;

  @Autowired private UnifiedAssetRepository unifiedAssetRepository;
  @Autowired private UnifiedAssetService unifiedAssetService;

  @SneakyThrows
  @Test
  void givenTargetMapper_whenPush_thenEventReported() {
    // mock
    doReturn(HttpResponse.ok(null)).when(pushServerAPIService).pushEvent(any());

    Page<UnifiedAssetEntity> assetEntityPage =
        unifiedAssetRepository.search(
            null,
            AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind(),
            null,
            PageRequest.of(0, 1000));

    if (assetEntityPage.isEmpty()) {
      YamlToolkit.parseYaml(
              readFileToString(
                  "deployment-config/components/api-targets-mappers/api-target-mapper.order.uni.add.yaml"),
              UnifiedAsset.class)
          .ifPresent(
              targetMapperAsset ->
                  unifiedAssetService.syncAsset(null, targetMapperAsset, new SyncMetadata(), true));
    }

    pushServerAPIService.runIt();
    verify(pushServerAPIService, times(2)).pushEvent(Mockito.any());
  }
}
