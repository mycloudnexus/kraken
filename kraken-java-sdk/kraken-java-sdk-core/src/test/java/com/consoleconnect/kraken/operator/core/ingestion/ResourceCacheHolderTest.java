package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.CustomConfig;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.SyncMetadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductReleaseDownloadFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.nio.charset.Charset;
import java.util.Objects;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ResourceCacheHolderTest extends AbstractIntegrationTest {
  public static final String UNI_ADD_SYNC_YAML =
      "memory:console-core/mef-lso-sonata-api/api-targets/api-target.quote.uni.add.sync.yaml";
  @Autowired ResourceCacheHolder resourceCacheHolder;
  @Autowired UnifiedAssetService unifiedAssetService;
  @Autowired ResourceLoaderFactory resourceLoaderFactory;

  @Test
  @Order(1)
  @SneakyThrows
  void givenDownloadAsset_whenGet_thenReturnData() {
    String id =
        unifiedAssetService
            .findByKind(AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind())
            .get(0)
            .getId();
    String string =
        IOUtils.toString(
            Objects.requireNonNull(
                ClassLoader.getSystemResourceAsStream("data/download-mapping-template.json")),
            Charset.defaultCharset());
    UnifiedAssetDto data =
        JsonToolkit.fromJson(string, new TypeReference<HttpResponse<UnifiedAssetDto>>() {})
            .getData();
    unifiedAssetService.syncAsset(
        id, data, new SyncMetadata("", "", DateTime.nowInUTCString()), true);
    resourceCacheHolder.loadCache(id);
    ProductReleaseDownloadFacets facets =
        UnifiedAsset.getFacets(data, ProductReleaseDownloadFacets.class);
    UnifiedAsset sourceProduct = resourceCacheHolder.findSourceProduct(facets);
    Optional<FileContentDescriptor> fileContentDescriptor =
        resourceLoaderFactory.readFile(
            resourceCacheHolder.appendTemplateIdToFullPath(id, UNI_ADD_SYNC_YAML));
    Assertions.assertTrue(fileContentDescriptor.isPresent());
    Assertions.assertNotNull(fileContentDescriptor.get());
    Assertions.assertNotNull(sourceProduct);
    Assertions.assertEquals(
        resourceCacheHolder.appendTemplateIdToFullPath(id, UNI_ADD_SYNC_YAML),
        fileContentDescriptor.get().getFullPath());
    resourceCacheHolder.clearCache();
  }
}
