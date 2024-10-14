package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.data.entity.AssetReleaseEntity;
import com.consoleconnect.kraken.operator.data.repo.AssetReleaseRepository;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class KrakenReleaseMonitorTest extends AbstractIntegrationTest {

  @Autowired private KrakenReleaseMonitor monitorService;
  @Autowired private AssetReleaseRepository assetReleaseRepository;

  @Test
  void test_deploymentCheck() {
    Assertions.assertNull(monitorService.getCurrentProductReleaseId());
    AssetReleaseEntity assetReleaseEntity = new AssetReleaseEntity();
    assetReleaseEntity.setVersion("1.0.0");
    assetReleaseEntity.setProductKey(UUID.randomUUID().toString());
    assetReleaseRepository.save(assetReleaseEntity);

    monitorService.checkRelease();
    Assertions.assertEquals(
        assetReleaseEntity.getVersion(), monitorService.getCurrentProductReleaseId());
  }
}
