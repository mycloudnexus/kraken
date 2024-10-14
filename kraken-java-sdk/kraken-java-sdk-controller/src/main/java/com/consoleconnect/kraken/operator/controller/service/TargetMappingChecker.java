package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_MAPPER;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.MAPPER_SIGN;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.MappingStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

public interface TargetMappingChecker {
  Set<String> keywords = Set.of("quote", "read", "sync");

  @Slf4j
  final class LogHolder {}

  default void checkEndpoint(ComponentAPITargetFacets.Endpoint endpoint, boolean skipCheck) {
    if (skipCheck) {
      return;
    }
    if ((StringUtils.isBlank(endpoint.getServerKey()) && StringUtils.isBlank(endpoint.getUrl()))
        || StringUtils.isBlank(endpoint.getPath())
        || StringUtils.isBlank(endpoint.getMethod())) {
      throw KrakenException.notFound(
          "Downstream API not found", new IllegalArgumentException("Downstream API not found"));
    }
    if (!isRequestValid(endpoint) || !isResponseValid(endpoint)) {
      throw new KrakenException(
          400,
          "API mapping is incomplete",
          new IllegalArgumentException("API mapping is incomplete"));
    }
  }

  default boolean isRequestValid(ComponentAPITargetFacets.Endpoint endpoint) {
    if (null == endpoint.getMappers().getRequest()
        || endpoint.getMappers().getRequest().isEmpty()) {
      return true;
    }
    return !CollectionUtils.isEmpty(endpoint.getMappers().getRequest())
        && endpoint.getMappers().getRequest().stream()
            .allMatch(
                mapper ->
                    !Boolean.TRUE.equals(mapper.getRequiredMapping())
                        // Check source emptiness
                        || (StringUtils.isNotEmpty(mapper.getSource())
                            && // Check target emptiness
                            StringUtils.isNotEmpty(mapper.getTarget())));
  }

  default boolean isResponseValid(ComponentAPITargetFacets.Endpoint endpoint) {
    if (null == endpoint.getMappers().getResponse()
        || endpoint.getMappers().getResponse().isEmpty()) {
      return true;
    }
    // Early return for null endpoint or empty mappers
    // Stream to check required mappers, target validity, and source/target emptiness
    return !CollectionUtils.isEmpty(endpoint.getMappers().getResponse())
        && endpoint.getMappers().getResponse().stream()
            .allMatch(
                mapper ->
                    !Boolean.TRUE.equals(mapper.getRequiredMapping())
                        || (StringUtils.isNotEmpty(mapper.getTarget())
                            && // Check target emptiness
                            StringUtils.isNotEmpty(mapper.getSource())
                            && // Check source emptiness
                            (!"enum".equalsIgnoreCase(mapper.getTargetType())
                                || (mapper.getDefaultValue() != null
                                    || (!mapper.getSource().startsWith("@{{")
                                        || !MapUtils.isEmpty(mapper.getValueMapping()))))));
  }

  default void fillMappingStatus(UnifiedAssetDto assetDto) {
    if (!COMPONENT_API_TARGET_MAPPER.getKind().equals(assetDto.getKind())) {
      return;
    }
    if (containsKeywords(assetDto.getMetadata().getKey())) {
      assetDto.setMappingStatus(MappingStatusEnum.COMPLETE.getDesc());
      return;
    }
    if (MapUtils.isEmpty(assetDto.getFacets())) {
      return;
    }
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
    boolean checkResult = true;
    try {
      checkEndpoint(mapperFacets.getEndpoints().get(0), false);
    } catch (KrakenException e) {
      checkResult = false;
    }
    if (checkResult) {
      assetDto.setMappingStatus(MappingStatusEnum.COMPLETE.getDesc());
    } else {
      assetDto.setMappingStatus(MappingStatusEnum.INCOMPLETE.getDesc());
    }
  }

  default String extractTargetKey(String targetMapperKey) {
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

  default boolean containsKeywords(String s) {
    if (StringUtils.isBlank(s)) {
      return false;
    }
    for (String word : keywords) {
      if (!s.contains(word)) {
        return false;
      }
    }
    return true;
  }
}
