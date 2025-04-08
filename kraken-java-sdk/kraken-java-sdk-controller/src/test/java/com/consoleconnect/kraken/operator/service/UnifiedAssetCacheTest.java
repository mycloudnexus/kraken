package com.consoleconnect.kraken.operator.service;

import com.consoleconnect.kraken.operator.config.TestApplication;
import com.consoleconnect.kraken.operator.controller.service.UnifiedAssetCache;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UnifiedAssetCacheTest extends AbstractIntegrationTest {

  @Autowired private UnifiedAssetCache unifiedAssetCache;

  @Test
  @Order(1)
  void givenNothing_whenInitialize_thenSuccess() {
    unifiedAssetCache.initialize();
    Assertions.assertNotNull(unifiedAssetCache.getMapperAssetMap());
  }
}
