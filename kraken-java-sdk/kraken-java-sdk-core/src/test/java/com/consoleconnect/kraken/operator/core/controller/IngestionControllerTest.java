package com.consoleconnect.kraken.operator.core.controller;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.helper.WebTestClientHelper;
import com.consoleconnect.kraken.operator.core.repo.AssetFacetRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.DataIngestionService;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class IngestionControllerTest extends AbstractIntegrationTest {

  private static final String ORG_ID = "ef90f7e7-dc7d-4538-aa5b-a4a7d8432da2";

  private final WebTestClientHelper testClientHelper;

  private final AssetFacetRepository assetFacetRepository;

  private final UnifiedAssetRepository assetRepository;

  private final DataIngestionService dataIngestionService;

  private final ApplicationEventPublisher applicationEventPublisher;

  @Autowired
  IngestionControllerTest(
      WebTestClient webTestClient,
      AssetFacetRepository assetFacetRepository,
      UnifiedAssetRepository assetRepository,
      DataIngestionService dataIngestionService,
      ApplicationEventPublisher applicationEventPublisher) {
    testClientHelper = new WebTestClientHelper(webTestClient);
    this.assetFacetRepository = assetFacetRepository;
    this.assetRepository = assetRepository;
    this.dataIngestionService = dataIngestionService;
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Order(1)
  @Test
  void testReloadData() {
    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path("/ingestion/reload").build(ORG_ID)),
        Assertions::assertNotNull);
  }

  @Order(2)
  @Test
  void testCleanAndReloadData() {
    this.assetFacetRepository.deleteAll();
    this.assetRepository.deleteAll();
    testClientHelper.postAndVerify(
        (uriBuilder -> uriBuilder.path("/ingestion/reload").build(ORG_ID)),
        Assertions::assertNotNull);
  }

  @Order(3)
  @Test
  void testIngestData() {
    IngestDataEvent event =
        new IngestDataEvent(ORG_ID, "deployment-config/kraken-product-sonata.yaml");
    dataIngestionService.ingestData(event);
    Assertions.assertTrue(true);
  }

  @Order(4)
  @Test
  void testIngestDataUserPublisher() {
    IngestDataEvent event =
        new IngestDataEvent(
            ORG_ID, "deployment-config/components/api-specs/productOrderManagement.api.yaml");
    applicationEventPublisher.publishEvent(event);
    Assertions.assertTrue(true);
  }
}
