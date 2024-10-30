package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.dto.ClientMapperVersionPayloadDto;
import com.consoleconnect.kraken.operator.controller.event.SingleMapperReportEvent;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.EnvironmentClientEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.repo.EnvironmentClientRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class EvnClientMapperInfoService {
  private final EnvironmentClientRepository environmentClientRepository;

  private final UnifiedAssetService unifiedAssetService;
  private final ProductDeploymentService productDeploymentService;

  @Transactional
  @EventListener(SingleMapperReportEvent.class)
  @Async
  public void handleSingleMapperInfo(SingleMapperReportEvent event) {
    String envId = event.envId();
    if (StringUtils.isBlank(event.version())) {
      productDeploymentService
          .retrieveApiMapperDeployments(
              envId,
              event.mapperKey(),
              DeployStatusEnum.SUCCESS,
              PageRequest.of(0, 100, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT))
          .getData()
          .stream()
          .findFirst()
          .ifPresent(
              dto ->
                  createOrUpdateVersionInfo(
                      envId,
                      event.mapperKey(),
                      dto.getVersion(),
                      dto.getSubVersion(),
                      dto.getTagId()));
    } else {
      unifiedAssetService
          .findBySpecification(
              Tuple2.ofList(
                  AssetsConstants.FIELD_KIND,
                  AssetKindEnum.COMPONENT_API_TARGET_MAPPER_TAG.getKind()),
              Tuple2.ofList(
                  LabelConstants.LABEL_VERSION_NAME,
                  event.version(),
                  LabelConstants.LABEL_SUB_VERSION_NAME,
                  event.subVersion(),
                  LabelConstants.MAPPER_KEY,
                  event.mapperKey()),
              null,
              null,
              null)
          .getData()
          .stream()
          .findFirst()
          .ifPresent(
              dto ->
                  createOrUpdateVersionInfo(
                      envId, event.mapperKey(), event.version(), event.subVersion(), dto.getId()));
    }
  }

  private void createOrUpdateVersionInfo(
      String envId, String mapperKey, String version, String subVersion, String tagId) {
    environmentClientRepository
        .findOneByEnvIdAndClientKeyAndKind(
            envId, mapperKey, ClientReportTypeEnum.CLIENT_MAPPER_VERSION.name())
        .or(
            () -> {
              EnvironmentClientEntity environmentClientEntity = new EnvironmentClientEntity();
              environmentClientEntity.setKind(ClientReportTypeEnum.CLIENT_MAPPER_VERSION.name());
              environmentClientEntity.setClientKey(mapperKey);
              environmentClientEntity.setEnvId(envId);
              ClientMapperVersionPayloadDto versionPayloadDto = new ClientMapperVersionPayloadDto();
              environmentClientEntity.setPayload(versionPayloadDto);
              return Optional.of(environmentClientEntity);
            })
        .ifPresent(
            environmentClientEntity -> {
              UnifiedAssetDto deployment =
                  unifiedAssetService
                      .findBySpecification(
                          Tuple2.ofList(
                              AssetsConstants.FIELD_KIND,
                              AssetKindEnum.PRODUCT_DEPLOYMENT.getKind()),
                          Tuple2.ofList(LabelConstants.LABEL_ENV_ID, envId),
                          List.of(tagId),
                          null,
                          null)
                      .getData()
                      .get(0);
              ClientMapperVersionPayloadDto versionPayloadDto =
                  JsonToolkit.fromJson(
                      JsonToolkit.toJson(
                          Optional.ofNullable(environmentClientEntity.getPayload())
                              .orElse(new ClientMapperVersionPayloadDto())),
                      ClientMapperVersionPayloadDto.class);
              versionPayloadDto.setMapperKey(mapperKey);
              versionPayloadDto.setVersion(version);
              versionPayloadDto.setSubVersion(subVersion);
              versionPayloadDto.setTagId(tagId);
              versionPayloadDto.setDeploymentId(deployment.getId());
              versionPayloadDto.setCreatedAt(deployment.getCreatedAt());
              versionPayloadDto.setCreatedBy(deployment.getCreatedBy());
              environmentClientEntity.setPayload(versionPayloadDto);
              environmentClientEntity.setUpdatedAt(ZonedDateTime.now());
              environmentClientRepository.save(environmentClientEntity);
            });
  }
}
