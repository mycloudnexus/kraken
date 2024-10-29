package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.EndPointUsageDTO;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.core.client.ServerAPIDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.PlaneTypeEnum;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.CaseFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public interface EndPointUsageCalculator {

  String UNDER_SCORE = "_";
  String CONTROL_PLANE_KEY =
      CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, PlaneTypeEnum.CONTROL_PLANE.name());
  String DATA_PLANE_STAGE_KEY =
      CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.LOWER_CAMEL,
          PlaneTypeEnum.DATA_PLANE.name() + UNDER_SCORE + EnvNameEnum.STAGE.name());
  String DATA_PLANE_PROD_KEY =
      CaseFormat.UPPER_UNDERSCORE.to(
          CaseFormat.LOWER_CAMEL,
          PlaneTypeEnum.DATA_PLANE.name() + UNDER_SCORE + EnvNameEnum.PRODUCTION.name());

  Map<String, String> ENV_TO_KEY_MAP =
      Map.of(
          EnvNameEnum.STAGE.name(),
          DATA_PLANE_STAGE_KEY,
          EnvNameEnum.PRODUCTION.name(),
          DATA_PLANE_PROD_KEY);

  UnifiedAssetService getUnifiedAssetService();

  EnvironmentClientRepository getEnvironmentClientRepository();

  default EndPointUsageDTO calculate(
      UnifiedAssetDto unifiedAssetDto, List<Environment> environments) {
    EndPointUsageDTO endPointUsageDTO = new EndPointUsageDTO();
    Map<String, List<UnifiedAssetDto>> endpointUsageMap =
        buildDefaultEndPointUsage(endPointUsageDTO);

    calculateControlPlane(unifiedAssetDto, endpointUsageMap);

    calculateDataPlane(unifiedAssetDto, endpointUsageMap, environments);

    return endPointUsageDTO;
  }

  default void calculateControlPlane(
      UnifiedAssetDto unifiedAssetDto, Map<String, List<UnifiedAssetDto>> endpointUsageMap) {
    String specKey = unifiedAssetDto.getMetadata().getKey();
    getUnifiedAssetService()
        .findByKind(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind())
        .forEach(
            assetDto -> {
              ComponentAPITargetFacets facets =
                  UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
              String serverKey = facets.getEndpoints().get(0).getServerKey();
              if (StringUtils.isNotBlank(specKey) && specKey.equals(serverKey)) {
                hiddenMappers(assetDto);
                endpointUsageMap.get(CONTROL_PLANE_KEY).add(assetDto);
              }
            });
  }

  default void calculateDataPlane(
      UnifiedAssetDto unifiedAssetDto,
      Map<String, List<UnifiedAssetDto>> endpointUsageMap,
      List<Environment> environments) {
    Map<String, Environment> environmentMap =
        environments.stream().collect(Collectors.toMap(Environment::getId, e -> e));
    String specKey = unifiedAssetDto.getMetadata().getKey();
    getEnvironmentClientRepository()
        .findAllByClientKeyAndKind(specKey, ClientReportTypeEnum.CLIENT_SERVER_API.name())
        .forEach(
            environmentClientEntity -> {
              Environment environment = environmentMap.get(environmentClientEntity.getEnvId());
              String countKey = generateCountKey(environment);
              if (StringUtils.isBlank(countKey)) {
                return;
              }
              String json = JsonToolkit.toJson(environmentClientEntity.getPayload());
              ServerAPIDto serverAPIDto =
                  JsonToolkit.fromJson(json, new TypeReference<ServerAPIDto>() {});
              List<String> mapperKeys = List.of(serverAPIDto.getMapperKey());
              List<UnifiedAssetDto> list =
                  getUnifiedAssetService().findByAllKeysIn(mapperKeys, true);
              list.forEach(this::hiddenMappers);
              endpointUsageMap.get(countKey).addAll(list);
            });
  }

  default void hiddenMappers(UnifiedAssetDto assetDto) {
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
    facets.getEndpoints().get(0).setMappers(null);
    assetDto.setFacets(
        JsonToolkit.fromJson(
            JsonToolkit.toJson(facets), new TypeReference<Map<String, Object>>() {}));
  }

  default String generateCountKey(Environment env) {
    if (Objects.isNull(env)) {
      return "";
    }
    return ENV_TO_KEY_MAP.getOrDefault(env.getName().toUpperCase(), "");
  }

  default Map<String, List<UnifiedAssetDto>> buildDefaultEndPointUsage(
      EndPointUsageDTO endPointUsageDTO) {
    Map<String, List<UnifiedAssetDto>> endpointUsageMap = endPointUsageDTO.getEndpointUsage();
    endpointUsageMap.put(CONTROL_PLANE_KEY, new ArrayList<>());
    endpointUsageMap.put(DATA_PLANE_STAGE_KEY, new ArrayList<>());
    endpointUsageMap.put(DATA_PLANE_PROD_KEY, new ArrayList<>());
    return endpointUsageMap;
  }
}
