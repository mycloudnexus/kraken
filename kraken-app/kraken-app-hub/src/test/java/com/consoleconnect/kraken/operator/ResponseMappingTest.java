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
import java.util.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class ResponseMappingTest extends AbstractIntegrationTest implements MappingTransformer {

  @SneakyThrows
  @ParameterizedTest
  @MethodSource(value = "buildExpectedAndInputList")
  void givenExpectedAndInput_whenValidateResponse_thenReturnOK(Pair<String, String> pair) {
    validate(pair.getLeft(), pair.getRight());
  }

  private void validate(String expected, String input) {
    UnifiedAsset asset = YamlToolkit.parseYaml(input, UnifiedAsset.class).get();
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    Assertions.assertNotNull(facets);

    StateValueMappingDto responseTargetMapperDto = new StateValueMappingDto();
    for (ComponentAPITargetFacets.Endpoint endpoint : facets.getEndpoints()) {
      String transformedResp = transform(endpoint, responseTargetMapperDto, new HashMap<>());
      log.info("expected    resp:{}", expected);
      log.info("transformed resp:{}", transformedResp);
      Assertions.assertEquals(expected, transformedResp);
    }
  }

  @SneakyThrows
  public static List<Pair<String, String>> buildExpectedAndInputList() {
    // Case-1: Address validation
    String expected1 = readCompactedFile("expected/expected-1-address.validation.json");
    String input1 =
        getTarget(
            "/mock/api-targets/api-target.address.validate.yaml",
            "/mock/api-targets-mappers/api-target-mapper.address.validate.yaml");
    Pair<String, String> pair1 = Pair.of(expected1, input1);

    // Case-2: Address retrieve
    String expected2 = readCompactedFile("expected/expected-2-address.retrieve.json");
    String input2 =
        getTarget(
            "/mock/api-targets/api-target.address.retrieve.yaml",
            "/mock/api-targets-mappers/api-target-mapper.address.retrieve.yaml");
    Pair<String, String> pair2 = Pair.of(expected2, input2);

    // Case-3: Eline Order Add
    String expected3 = readCompactedFile("expected/expected-order.json");
    String input3 =
        getTarget(
            "/mock/api-targets/api-target.order.eline.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.order.eline.add.yaml");
    Pair<String, String> pair3 = Pair.of(expected3, input3);

    // Case-4: UNI Order Add
    String input4 =
        getTarget(
            "/mock/api-targets/api-target.order.uni.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.order.uni.add.yaml");
    Pair<String, String> pair4 = Pair.of(expected3, input4);

    // Case-5: Eline Quote Add
    String expected5 = readCompactedFile("expected/expected-quote.eline.add.json");
    String input5 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.add.yaml");
    Pair<String, String> pair5 = Pair.of(expected5, input5);

    // Case-6: UNI Quote Add
    String expected6 = readCompactedFile("expected/expected-quote.uni.add.json");
    String input6 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.add.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.add.yaml");
    Pair<String, String> pair6 = Pair.of(expected6, input6);

    // Case-7: Eline Quote Read
    String expected7 = readCompactedFile("expected/expected-7-quote.eline.read.json");
    String input7 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.read.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.read.yaml");
    Pair<String, String> pair7 = Pair.of(expected7, input7);

    // Case-8: UNI Quote Read
    String input8 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.read.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.read.yaml");
    Pair<String, String> pair8 = Pair.of(expected7, input8);

    // Case-9: UNI quote Add Sync
    String expected9 = readCompactedFile("expected/expected-9-quote.uni.add.sync.json");
    String input9 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.add.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.add.sync.yaml");
    Pair<String, String> pair9 = Pair.of(expected9, input9);

    // Case-10: UNI Quote Read Sync
    String expected10 = readCompactedFile("expected/expected-10-quote.uni.read.sync.json");
    String input10 =
        getTarget(
            "/mock/api-targets/api-target.quote.uni.read.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.uni.read.sync.yaml");
    Pair<String, String> pair10 = Pair.of(expected10, input10);

    // Case-11: Eline Quote Add Sync
    String expected11 = readCompactedFile("expected/expected-11-quote.eline.add.sync.json");
    String input11 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.add.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.add.sync.yaml");
    Pair<String, String> pair11 = Pair.of(expected11, input11);

    // Case-12: Eline Quote Read Sync
    String input12 =
        getTarget(
            "/mock/api-targets/api-target.quote.eline.read.sync.yaml",
            "/mock/api-targets-mappers/api-target-mapper.quote.eline.read.sync.yaml");
    Pair<String, String> pair12 = Pair.of(expected10, input12);

    List<Pair<String, String>> list = new ArrayList<>();
    list.add(pair1);
    list.add(pair2);
    list.add(pair3);
    list.add(pair4);
    list.add(pair5);
    list.add(pair6);
    list.add(pair7);
    list.add(pair8);
    list.add(pair9);
    list.add(pair10);
    list.add(pair11);
    list.add(pair12);

    return list;
  }

  @SneakyThrows
  public static String readCompactedFile(String path) {
    return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
        readFileToString(path));
  }

  public static String getTarget(String targetApiPath, String mapperApiPath) throws IOException {
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
