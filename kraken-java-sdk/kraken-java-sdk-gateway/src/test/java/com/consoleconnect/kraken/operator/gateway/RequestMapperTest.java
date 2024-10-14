package com.consoleconnect.kraken.operator.gateway;

import static com.consoleconnect.kraken.operator.gateway.SpELEngineTest.readFileToString;
import static org.mockito.ArgumentMatchers.anyString;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.LoadTargetAPIConfigActionRunner;
import com.consoleconnect.kraken.operator.gateway.service.RenderRequestService;
import java.io.IOException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@Slf4j
class RequestMapperTest {

  private static UnifiedAssetService unifiedAssetService;
  private static RenderRequestService renderRequestService;
  private static LoadTargetAPIConfigActionRunner runner;

  @BeforeAll
  static void init() {
    unifiedAssetService = Mockito.mock(UnifiedAssetService.class);
    renderRequestService = new RenderRequestService(unifiedAssetService);
    runner =
        new LoadTargetAPIConfigActionRunner(
            new AppProperty(), unifiedAssetService, renderRequestService);
  }

  @Test
  void requestParserTest() throws IOException {
    ComponentAPITargetFacets facets = renderFacets("mockData/api-target.address.validate.yaml");
    runner.encodeUrlParam(facets.getEndpoints().get(0).getPath());
    System.out.println("test address validation");
    Assertions.assertTrue(facets.getEndpoints().get(0).getPath().contains("criteria"));

    System.out.println("test order add");
    ComponentAPITargetFacets orderFacets = renderFacets("mockData/api-target.order.eline.add.yaml");
    Assertions.assertTrue(orderFacets.getEndpoints().get(0).getPath().contains("mefQuery"));

    System.out.println("test notify");
    UnifiedAssetDto notifyAsset =
        YamlToolkit.parseYaml(
                readFileToString("mockData/api-target.order.notification.state.change.yaml"),
                UnifiedAssetDto.class)
            .get();
    Mockito.doReturn(notifyAsset).when(unifiedAssetService).findOne(anyString());
    ComponentAPITargetFacets readFacets =
        renderFacets("mockData/api-target.order.notification.state.change.yaml");
    Assertions.assertTrue(
        readFacets.getEndpoints().get(0).getRequestBody().contains("mefRequestBody.id"));
  }

  private ComponentAPITargetFacets renderFacets(String path) throws IOException {
    String s = readFileToString(path);
    Optional<UnifiedAsset> unifiedAsset = YamlToolkit.parseYaml(s, UnifiedAsset.class);
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(unifiedAsset.get(), ComponentAPITargetFacets.class);
    renderRequestService.parseRequest(facets);
    return facets;
  }
}
