package com.consoleconnect.kraken.operator.gateway;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.MAPPER_SIGN;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MappingTransformerTest extends AbstractIntegrationTest implements MappingTransformer {

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

  @SneakyThrows
  public static String readCompactedFile(String path) {
    return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
        readFileToString(path));
  }

  public static UnifiedAsset getTarget(String targetApiPath, String mapperApiPath)
      throws IOException {
    Optional<UnifiedAsset> unifiedAsset =
        YamlToolkit.parseYaml(readFileToString(targetApiPath), UnifiedAsset.class);
    Optional<UnifiedAsset> mapperAssetOpt =
        YamlToolkit.parseYaml(readFileToString(mapperApiPath), UnifiedAsset.class);

    UnifiedAsset targetAsset = unifiedAsset.get();
    UnifiedAsset targetMapperAsset = mapperAssetOpt.get();
    String targetKey = extractTargetKey(targetMapperAsset.getMetadata().getKey());
    Assertions.assertEquals(targetAsset.getMetadata().getKey(), targetKey);

    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(targetAsset, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(targetMapperAsset, ComponentAPITargetFacets.class);
    facets.getEndpoints().get(0).setPath(mapperFacets.getEndpoints().get(0).getPath());
    facets.getEndpoints().get(0).setMethod(mapperFacets.getEndpoints().get(0).getMethod());
    facets.getEndpoints().get(0).setMappers(mapperFacets.getEndpoints().get(0).getMappers());
    targetAsset.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
    return targetAsset;
  }

  public static String extractTargetKey(String targetMapperKey) {
    if (StringUtils.isBlank(targetMapperKey)) {
      return "";
    }
    int loc = targetMapperKey.indexOf(MAPPER_SIGN);
    if (loc < 0) {
      return "";
    }
    if (loc + MAPPER_SIGN.length() == targetMapperKey.length()) {
      return targetMapperKey.substring(0, loc);
    } else {
      return targetMapperKey.substring(0, loc)
          + targetMapperKey.substring(loc + MAPPER_SIGN.length());
    }
  }
}
