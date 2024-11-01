package com.consoleconnect.kraken.operator;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.MAPPER_SIGN;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class ResponseMappingTest extends AbstractIntegrationTest implements MappingTransformer {

  @SneakyThrows
  @Test
  void testResponseMapper() {
    String expected1 = readCompactedFile("expected/expected-1-address.validation.json");
    String input =
        getTarget(
            "/mock/api-targets/api-target.address.validate.yaml",
            "/mock/api-targets-mappers/api-target-mapper.address.validate.yaml");
    validate(expected1, input);

    String expected2 = readCompactedFile("expected/expected-2-address.retrieve.json");
    String input2 =
        getTarget(
            "/mock/api-targets/api-target.address.retrieve.yaml",
            "/mock/api-targets-mappers/api-target-mapper.address.retrieve.yaml");
    validate(expected2, input2);

    String expectedOrder = readCompactedFile("expected/expected-order.json");
    String input3 =
        getTarget(
            "/mock/api-targets/api-target.order.eline.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.order.eline.add.yaml");
    validate(expectedOrder, input3);

    String input4 =
        getTarget(
            "/mock/api-targets/api-target.order.uni.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.order.uni.add.yaml");
    validate(expectedOrder, input4);

    String expectedQuote = readCompactedFile("expected/expected-quote.eline.add.json");
    String input5 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.add.yaml");
    validate(expectedQuote, input5);

    expectedQuote = readCompactedFile("expected/expected-quote.uni.add.json");
    String input6 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.add.yaml");
    validate(expectedQuote, input6);

    String expected7 = readCompactedFile("expected/expected-7-quote.eline.read.json");
    String input7 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.read.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.read.yaml");
    validate(expected7, input7);

    String input8 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.read.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.read.yaml");
    validate(expected7, input8);

    String expected9 = readCompactedFile("expected/expected-9-quote.uni.add.sync.json");
    String input9 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.add.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml");
    validate(expected9, input9);

    String expected10 = readCompactedFile("expected/expected-10-quote.uni.read.sync.json");
    String input10 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.read.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.read.sync.yaml");
    validate(expected10, input10);

    String input11 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.add.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.add.sync.yaml");
    validate(expected9, input11);

    String input12 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.read.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.read.sync.yaml");
    validate(expected10, input12);
  }

  public void validate(String expected, String input) {
    UnifiedAsset asset = YamlToolkit.parseYaml(input, UnifiedAsset.class).get();
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    Assertions.assertNotNull(facets);

    StateValueMappingDto responseTargetMapperDto = new StateValueMappingDto();
    for (ComponentAPITargetFacets.Endpoint endpoint : facets.getEndpoints()) {
      String transformedResp = transform(endpoint, responseTargetMapperDto);
      log.info("expected    resp:{}", expected);
      log.info("transformed resp:{}", transformedResp);
      Assertions.assertEquals(expected, transformedResp);
    }
  }

  @SneakyThrows
  public String readCompactedFile(String path) {
    return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
        readFileToString(path));
  }

  public String getTarget(String targetApiPath, String mapperApiPath) throws IOException {
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
    return JsonToolkit.toJson(targetAsset);
  }

  public String extractTargetKey(String targetMapperKey) {
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
