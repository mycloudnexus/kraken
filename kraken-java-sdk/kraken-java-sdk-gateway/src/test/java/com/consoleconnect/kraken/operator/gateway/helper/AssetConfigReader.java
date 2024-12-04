package com.consoleconnect.kraken.operator.gateway.helper;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.MAPPER_SIGN;

import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

public class AssetConfigReader extends AbstractIntegrationTest {

  @SneakyThrows
  public String readCompactedFile(String path) {
    return com.consoleconnect.kraken.operator.core.toolkit.StringUtils.compact(
        readFileToString(path));
  }

  public UnifiedAsset getTarget(String targetApiPath, String mapperApiPath) throws IOException {
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
