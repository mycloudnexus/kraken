package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.nimbusds.jwt.JWTParser;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles(value = "buyer-check")
public class BuyerCheckServiceTest extends AbstractIntegrationTest {
  public static final String BUYER_02 = "buyer02";
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired BuyerCheckerService buyerCheckerService;
  @Autowired WebTestClient webTestClient;
  @Autowired AuthDataProperty.ResourceServer resourceServer;
  private final String xKrakenKeyToken =
      "eyJhbGciOiJIUzI1NiIsImtpZCI6ImtyYWtlbiIsInR5cCI6IkpXVCJ9.eyJleHAiOjQ0Mzg1Mzc5NDIsImlhdCI6MTcyNzMyODM0MiwiaXNzIjoiaHR0cHM6Ly9rcmFrZW4uY29uc29sZWNvbm5lY3QuY29tL2lzc3VlciIsInN1YiI6ImJ1eWVyMDIifQ.9FL7_ph6-NtJg5hNSOqt8HaxwMg66gnEjpb_XCMsXrY";

  @Test
  @Order(1)
  @SneakyThrows
  void givenBuyer_whenInit_thenReturnData() {
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(AssetKindEnum.PRODUCT_BUYER.getKind(), "buyer02", "buyer02");
    unifiedAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());

    Instant instant = JWTParser.parse(xKrakenKeyToken).getJWTClaimsSet().getIssueTime().toInstant();
    unifiedAsset
        .getMetadata()
        .getLabels()
        .put(
            LabelConstants.LABEL_ISSUE_AT,
            DateTimeFormatter.ISO_INSTANT.format(instant.minusSeconds(60)));
    unifiedAsset.getMetadata().getLabels().put(LabelConstants.LABEL_BUYER_ID, BUYER_02);
    IngestionDataResult ingestionDataResult =
        unifiedAssetService.syncAsset(
            "product.mef.sonata.api",
            unifiedAsset,
            new SyncMetadata("", "", DateTime.nowInUTCString(), ""),
            true);
    Assertions.assertEquals(200, ingestionDataResult.getCode());
  }
}
