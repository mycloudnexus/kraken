package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_CREATE_AT;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.*;
import static com.consoleconnect.kraken.operator.core.toolkit.LabelConstants.LABEL_ENV_ID;

import com.consoleconnect.kraken.operator.controller.dto.ApiMapperDeploymentDTO;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import java.util.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public interface LatestDeploymentCalculator {
  double DEFAULT_VERSION = 1.0;
  double INIT_VERSION = 0.1;
  String PRODUCTION_ENV = "production";

  UnifiedAssetService getUnifiedAssetService();

  default Optional<UnifiedAssetDto> queryLatestSuccessDeploymentAsset(
      String mapperKey, String envId) {
    List<Tuple2> eqConditions = new ArrayList<>();
    eqConditions.add(Tuple2.of(KIND, AssetKindEnum.PRODUCT_DEPLOYMENT.getKind()));
    eqConditions.add(Tuple2.of(STATUS, DeployStatusEnum.SUCCESS.name()));
    List<Tuple2> labelConditions = new ArrayList<>();
    labelConditions.add(Tuple2.of(LABEL_RELEASE_KIND, ReleaseKindEnum.API_LEVEL.getKind()));
    if (StringUtils.isNotBlank(envId)) {
      labelConditions.add(Tuple2.of(LABEL_ENV_ID, envId));
    }
    if (StringUtils.isNotBlank(mapperKey)) {
      labelConditions.add(Tuple2.of(mapperKey, Boolean.TRUE.toString()));
    }
    Paging<UnifiedAssetDto> deployments =
        getUnifiedAssetService()
            .findBySpecification(
                eqConditions,
                labelConditions,
                null,
                PageRequest.of(0, 10, Sort.Direction.DESC, FIELD_CREATE_AT),
                null);
    return deployments.getData().stream().findFirst();
  }

  default Optional<UnifiedAssetDto> queryLatestDeploymentAsset(
      String mapperKey, String envId, List<Tuple2> inConditions) {
    List<Tuple2> eqConditions = new ArrayList<>();
    eqConditions.add(Tuple2.of(KIND, AssetKindEnum.PRODUCT_DEPLOYMENT.getKind()));
    List<Tuple2> labelConditions = new ArrayList<>();
    labelConditions.add(Tuple2.of(LABEL_RELEASE_KIND, ReleaseKindEnum.API_LEVEL.getKind()));
    if (StringUtils.isNotBlank(envId)) {
      labelConditions.add(Tuple2.of(LABEL_ENV_ID, envId));
    }
    if (StringUtils.isNotBlank(mapperKey)) {
      labelConditions.add(Tuple2.of(mapperKey, Boolean.TRUE.toString()));
    }
    Paging<UnifiedAssetDto> deployments =
        getUnifiedAssetService()
            .findBySpecification(
                eqConditions,
                labelConditions,
                null,
                PageRequest.of(0, 10, Sort.Direction.DESC, FIELD_CREATE_AT),
                inConditions);
    return deployments.getData().stream().findFirst();
  }

  default double computeMaximumRunningVersion(
      UnifiedAssetDto deploymentAssetDto, String mapperKey, String envId) {
    String status = deploymentAssetDto.getMetadata().getStatus();
    if (DeployStatusEnum.SUCCESS.name().equals(status)) {
      return computeMaximumRunningVersion(deploymentAssetDto);
    } else {
      return DeployStatusEnum.FAILED.name().equals(status)
          ? queryLatestSuccessDeploymentAsset(mapperKey, envId)
              .map(this::computeMaximumRunningVersion)
              .orElse(INIT_VERSION)
          : INIT_VERSION;
    }
  }

  default double computeMaximumRunningVersion(UnifiedAssetDto latestDeploymentAsset) {
    return latestDeploymentAsset.getMetadata().getTags().stream()
        .map(tag -> getUnifiedAssetService().findOneByIdOrKey(tag))
        .map(
            tagItem ->
                Double.parseDouble(
                    tagItem
                        .getLabels()
                        .getOrDefault(LABEL_VERSION_NAME, String.valueOf(DEFAULT_VERSION))))
        .max(Double::compare)
        .orElse(INIT_VERSION);
  }

  default void calculateCanDeployToTargetEnv(ApiMapperDeploymentDTO deploymentDTO) {
    if (PRODUCTION_ENV.equals(deploymentDTO.getEnvName())
        || !DeployStatusEnum.SUCCESS.name().equals(deploymentDTO.getStatus())
        || !deploymentDTO.isVerifiedStatus()
        || queryLatestSuccessDeploymentAsset(
                    deploymentDTO.getTargetMapperKey(), deploymentDTO.getEnvId())
                .map(this::computeMaximumRunningVersion)
                .orElse(INIT_VERSION)
            >= (StringUtils.isBlank(deploymentDTO.getVersion())
                ? DEFAULT_VERSION
                : Double.parseDouble(deploymentDTO.getVersion()))) {
      return;
    }
    deploymentDTO.setProductionEnable(true);
  }
}
