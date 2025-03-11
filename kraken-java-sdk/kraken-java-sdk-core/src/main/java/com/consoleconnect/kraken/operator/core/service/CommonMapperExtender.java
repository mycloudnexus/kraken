package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.CUSTOMIZED_PLACE_HOLDER;
import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.extractMapperParam;

import com.consoleconnect.kraken.operator.core.model.CommonMapperRef;
import com.consoleconnect.kraken.operator.core.model.KVPair;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public interface CommonMapperExtender extends AssetReader {

  default void extendCommonMapper(ComponentAPITargetFacets.Endpoint endpointNew) {
    if (Objects.isNull(endpointNew) || Objects.isNull(endpointNew.getMappers())) {
      return;
    }
    CommonMapperRef schemaRef = endpointNew.getMappers().getSchemaRef();
    if (Objects.isNull(schemaRef) || StringUtils.isBlank(schemaRef.getRef())) {
      return;
    }
    String refPath = schemaRef.getRef();
    Optional<UnifiedAsset> commonAssetOpt = readFromPath(refPath);
    commonAssetOpt.ifPresent(
        commonAsset -> {
          ComponentAPITargetFacets commonFacet =
              UnifiedAsset.getFacets(commonAsset, ComponentAPITargetFacets.class);
          if (Objects.isNull(commonFacet) || CollectionUtils.isEmpty(commonFacet.getEndpoints())) {
            return;
          }
          ComponentAPITargetFacets.Endpoint commonEndpoint = commonFacet.getEndpoints().get(0);
          if (Objects.isNull(commonEndpoint.getMappers())) {
            return;
          }
          endpointNew
              .getMappers()
              .setRequest(
                  restoreMapper(
                      commonEndpoint.getMappers().getRequest(),
                      schemaRef.getParams(),
                      endpointNew.getMappers().getRequest()));

          endpointNew
              .getMappers()
              .setResponse(
                  restoreMapper(
                      commonEndpoint.getMappers().getResponse(),
                      schemaRef.getParams(),
                      endpointNew.getMappers().getResponse()));
        });
  }

  default List<ComponentAPITargetFacets.Mapper> restoreMapper(
      List<ComponentAPITargetFacets.Mapper> commonMappers,
      List<KVPair> params,
      List<ComponentAPITargetFacets.Mapper> specificMappers) {
    if (CollectionUtils.isEmpty(commonMappers) || CollectionUtils.isEmpty(params)) {
      return List.of();
    }
    Map<String, String> paramMap =
        params.stream()
            .collect(
                Collectors.toMap(
                    KVPair::getKey, KVPair::getVal, (existing, replacement) -> existing));

    if (Objects.isNull(specificMappers)) {
      specificMappers = new ArrayList<>();
    }
    for (ComponentAPITargetFacets.Mapper mapper : commonMappers) {
      if (Objects.isNull(mapper)) {
        continue;
      }
      // To update values based on paramMap
      BiConsumer<String, Consumer<String>> updateValue =
          (original, setter) -> {
            List<String> extractedParams = extractMapperParam(original);
            if (CollectionUtils.isNotEmpty(extractedParams)) {
              String replacement = paramMap.getOrDefault(extractedParams.get(0), "");
              setter.accept(original.replaceAll(CUSTOMIZED_PLACE_HOLDER, replacement));
            }
          };

      updateValue.accept(mapper.getName(), mapper::setName);
      updateValue.accept(mapper.getConvertValue(), mapper::setConvertValue);

      // Ensure unique entries in `specificMappers`
      specificMappers.removeIf(item -> item.getName().equals(mapper.getName()));
      specificMappers.add(mapper);
    }
    return List.copyOf(specificMappers);
  }
}
