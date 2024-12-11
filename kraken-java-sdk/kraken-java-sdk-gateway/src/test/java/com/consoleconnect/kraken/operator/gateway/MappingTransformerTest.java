package com.consoleconnect.kraken.operator.gateway;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.helper.AssetConfigReader;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.HashMap;
import java.util.Map;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MappingTransformerTest extends AssetConfigReader implements MappingTransformer {

  @Test
  void givenJsonInput_whenDeleteNode_thenReturnOK() {
    Map<String, String> checkPathMap = new HashMap<>();
    checkPathMap.put("$.key1", "$.key1");
    checkPathMap.put("$.key2", "$.key2");
    checkPathMap.put("$.key3", "$.key3");
    String input = "{\"key1\":\"\",\"key2\":0,\"key3\":false,\"key\":\"hello kraken\"}";
    String result = deleteNodeByPath(checkPathMap, input);
    Assertions.assertEquals("{\"key\":\"hello kraken\"}", result);
  }

  @Test
  void givenJsonArray_whenCounting_thenReturnOK() {
    String pathExpression = "$.quoteItem[*].requestedQuoteItemTerm.duration.units";
    String jsonData =
        "{\"quoteItem\":[{\"quoteItemTerm\":[{\"duration\":{\"amount\":1,\"units\":\"calendarMonths\"}}]}]}";
    Map<String, Object> quoteItemMap =
        JsonToolkit.fromJson(jsonData, new TypeReference<Map<String, Object>>() {});
    int result = lengthOfArrayNode(pathExpression, quoteItemMap);
    System.out.println(result);
    Assertions.assertTrue(result > -1);
  }

  @Test
  void givenJson_whenDeleteNodeByPath_thenDeleteNodeSuccess() {
    Map<String, String> checkPathMap = new HashMap<>();
    String input =
        "{\"state\":\"completed1\",\"completionDate\":\"123\",\"productOrderItem\":[{\"state\":\"completed\",\"completionDate\":\"123\"}]}\n";
    checkPathMap.put("$[?(@.state == 'completed')]", "$.completionDate");
    checkPathMap.put(
        "$.productOrderItem[?(@.state == 'completed')]",
        "$.productOrderItem[?(@.state != 'completed')].completionDate");
    checkPathMap.put("$.notFound", "$.notFound");
    String s = deleteNodeByPath(checkPathMap, input);
    assertThat(s, hasJsonPath("$.productOrderItem[0].completionDate"), notNullValue());
  }

  @SneakyThrows
  @Test
  void givenArrayRequest_whenTransform_thenReturnOK() {
    Map<String, Object> map =
        JsonToolkit.fromJson(
            readFileToString("mockData/quoteArrayRequest.json"),
            new TypeReference<Map<String, Object>>() {});
    Map<String, Object> inputs = new HashMap<>();
    inputs.put("mefRequestBody", map);
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    UnifiedAsset targetAsset =
        getTarget(
            "deployment-config/components/api-targets/api-target.quote.uni.add.sync.yaml",
            "deployment-config/components/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml");
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(targetAsset, ComponentAPITargetFacets.class);
    facets
        .getEndpoints()
        .forEach(
            endpoint -> {
              String transformedResp = transform(endpoint, stateValueMappingDto, inputs);
              String expectedResult = readCompactedFile("mockData/expected-quote-array-resp.json");
              Assertions.assertEquals(expectedResult, transformedResp);
            });
  }
}
