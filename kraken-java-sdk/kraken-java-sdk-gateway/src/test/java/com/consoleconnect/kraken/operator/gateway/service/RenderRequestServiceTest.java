package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.helper.AssetConfigReader;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ContextConfiguration;

@Slf4j
@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class RenderRequestServiceTest extends AssetConfigReader {

  @Autowired private UnifiedAssetService unifiedAssetService;

  @SpyBean RenderRequestService renderRequestService;

  @SneakyThrows
  @Test
  void givenArrayItems_whenHandleBody_thenReturnOK() {
    Map<String, Object> map =
        JsonToolkit.fromJson(
            readFileToString("mockData/quoteArrayRequest.json"),
            new TypeReference<Map<String, Object>>() {});
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("body", map);
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    UnifiedAsset targetAsset =
        getTarget(
            "deployment-config/components/api-targets/api-target.quote.uni.add.sync.yaml",
            "deployment-config/components/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml");
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(targetAsset, ComponentAPITargetFacets.class);
    renderRequestService.handleBody(facets, stateValueMappingDto, inputs);
    String transformedRequest =
        com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
            facets.getEndpoints().get(0).getRequestBody());
    String expectedResult = readCompactedFile("mockData/expected-quote-array-request.json");
    Assertions.assertEquals(expectedResult, transformedRequest);
  }
}
