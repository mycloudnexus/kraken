package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.sync.CustomConfig;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.charset.Charset;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

@MockIntegrationTest
@ContextConfiguration(classes = {CustomConfig.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestPropertySource(properties = {"app.mgmt.mgmtServerEnabled=true"})
class MgmtPullTemplateServiceTest extends AbstractIntegrationTest {
  public static final String RELEASE_1_1_0_PUBLISHED = "mef.sonata.release@1.1.0.published";
  public static final String RELEASE_1_1_0_DOWNLOAD = "mef.sonata.release@1.1.0.download";
  @SpyBean private MgmtPullTemplateService mgmtPullTemplateService;
  @Autowired SyncProperty syncProperty;
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired DataIngestionJob dataIngestionJob;
  @Autowired AppProperty appProperty;

  @Test
  @Order(1)
  @SneakyThrows
  void givenAvailableProductRelease_whenQueryLatestProductRelease_thenReturnData() {
    appProperty.getTenant().setWorkspacePath("classpath:/deployment-config/kraken.yaml");
    dataIngestionJob.ingestionWorkspace();
    String string =
        IOUtils.toString(
            ClassLoader.getSystemResourceAsStream("data/latest-product-release.json"),
            Charset.defaultCharset());
    UnifiedAssetDto unifiedAssetDto = JsonToolkit.fromJson(string, UnifiedAssetDto.class);
    Mockito.doReturn(HttpResponse.ok(unifiedAssetDto))
        .when(mgmtPullTemplateService)
        .blockCurl(
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));
    mgmtPullTemplateService.queryLatestProductRelease();
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(RELEASE_1_1_0_PUBLISHED);
    Assertions.assertNotNull(assetDto);
  }

  @Test
  @Order(2)
  @SneakyThrows
  void givenExistedPublishedAsset_whenPullMappingTemplateDetails_thenOk() {
    String string =
        IOUtils.toString(
            ClassLoader.getSystemResourceAsStream("data/download-mapping-template.json"),
            Charset.defaultCharset());
    UnifiedAssetDto unifiedAssetDto =
        JsonToolkit.fromJson(string, new TypeReference<HttpResponse<UnifiedAssetDto>>() {})
            .getData();
    Mockito.doReturn(HttpResponse.ok(unifiedAssetDto))
        .when(mgmtPullTemplateService)
        .blockCurl(
            Mockito.eq(HttpMethod.GET),
            Mockito.any(),
            Mockito.isNull(),
            Mockito.any(ParameterizedTypeReference.class));
    mgmtPullTemplateService.pullMappingTemplateDetails();
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(RELEASE_1_1_0_DOWNLOAD);
    Assertions.assertNotNull(assetDto);
  }
}
