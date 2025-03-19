package com.consoleconnect.kraken.operator.core.service;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
class AssetReaderTest extends AbstractIntegrationTest implements AssetReader {

  @Autowired private ResourceLoaderFactory resourceLoaderFactory;

  @Override
  public ResourceLoaderFactory getResourceLoaderFactory() {
    return this.resourceLoaderFactory;
  }

  @Test
  void givenFullPath_whenReading_thenReturnOK() {
    String fullPath = "classpath:/deployment-config/kraken.yaml";
    Optional<UnifiedAsset> resultOpt = readFromPath(fullPath);
    Assertions.assertTrue(resultOpt.isPresent());
  }
}
