package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.entity.EnvironmentEntity;
import com.consoleconnect.kraken.operator.controller.enums.SystemStateEnum;
import com.consoleconnect.kraken.operator.controller.mapper.SystemInfoMapper;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.repo.EnvironmentRepository;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.SystemInfoEntity;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.repo.SystemInfoRepository;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SystemInfoService {

  public static final String KEY = "CONTROL_PLANE";

  private final UnifiedAssetService unifiedAssetService;
  private final SystemInfoRepository systemInfoRepository;
  private final MgmtProperty mgmtProperty;
  private final EnvironmentRepository environmentRepository;
  private final UnifiedAssetRepository unifiedAssetRepository;

  @Value("${spring.build.version}")
  private String buildVersion;

  @Value("${spring.build.api-spec-version:Haley}")
  private String apiSpecVersion;

  @EventListener(PlatformSettingCompletedEvent.class)
  @Transactional
  @Async
  public void initialize() {
    List<UnifiedAssetDto> list = unifiedAssetService.findByKind(AssetKindEnum.PRODUCT.getKind());
    String productKey =
        Optional.ofNullable(list.get(0))
            .map(UnifiedAssetDto::getMetadata)
            .map(Metadata::getKey)
            .orElse(null);
    String productSpec = apiSpecVersion;
    systemInfoRepository
        .findOneByKey(KEY)
        .or(
            () -> {
              SystemInfoEntity systemInfoEntity = new SystemInfoEntity();
              systemInfoEntity.setKey(KEY);
              systemInfoEntity.setStatus(SystemStateEnum.RUNNING.name());
              initProductVersion(systemInfoEntity);
              return Optional.of(systemInfoEntity);
            })
        .ifPresent(
            systemInfoEntity -> {
              systemInfoEntity.setControlAppVersion(buildVersion);
              systemInfoEntity.setProductKey(productKey);
              systemInfoEntity.setProductSpec(productSpec);
              systemInfoRepository.save(systemInfoEntity);
            });
  }

  private void initProductVersion(SystemInfoEntity systemInfoEntity) {
    List<EnvironmentEntity> environmentEntities = environmentRepository.findAll();
    String stageEnvId =
        environmentEntities.stream()
            .filter(t -> EnvNameEnum.STAGE.name().equalsIgnoreCase(t.getName()))
            .findFirst()
            .orElseThrow()
            .getId()
            .toString();
    String productionEnvId =
        environmentEntities.stream()
            .filter(t -> EnvNameEnum.PRODUCTION.name().equalsIgnoreCase(t.getName()))
            .findFirst()
            .orElseThrow()
            .getId()
            .toString();
    AtomicReference<String> firstVersion = new AtomicReference<>();
    String templateVersion =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(
                    AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
                null,
                null,
                PageRequest.of(0, 1, Sort.Direction.ASC, AssetsConstants.FIELD_CREATE_AT),
                null)
            .getData()
            .stream()
            .findFirst()
            .map(
                t -> {
                  firstVersion.set(t.getId());
                  return t.getMetadata().getLabels().get(LabelConstants.LABEL_PRODUCT_VERSION);
                })
            .orElse(null);
    String controlVersion =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(
                    AssetsConstants.FIELD_KIND,
                    AssetKindEnum.PRODUCT_TEMPLATE_CONTROL_DEPLOYMENT.getKind()),
                null,
                null,
                PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT),
                null)
            .getData()
            .stream()
            .findFirst()
            .map(t -> t.getMetadata().getLabels().get(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID))
            .map(unifiedAssetService::findOne)
            .map(dto -> dto.getMetadata().getLabels().get(LabelConstants.LABEL_PRODUCT_VERSION))
            .map(Constants::formatVersionUsingV)
            .orElse(null);
    String stageVersion =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(
                    AssetsConstants.FIELD_KIND,
                    AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
                Tuple2.ofList(LabelConstants.LABEL_ENV_ID, stageEnvId),
                null,
                PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT),
                null)
            .getData()
            .stream()
            .findFirst()
            .map(t -> t.getMetadata().getLabels().get(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID))
            .map(unifiedAssetService::findOne)
            .map(dto -> dto.getMetadata().getLabels().get(LabelConstants.LABEL_PRODUCT_VERSION))
            .map(Constants::formatVersionUsingV)
            .orElse(null);
    String productionVersion =
        unifiedAssetService
            .findBySpecification(
                Tuple2.ofList(
                    AssetsConstants.FIELD_KIND,
                    AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
                Tuple2.ofList(LabelConstants.LABEL_ENV_ID, productionEnvId),
                null,
                PageRequest.of(0, 1, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT),
                null)
            .getData()
            .stream()
            .findFirst()
            .map(t -> t.getMetadata().getLabels().get(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID))
            .map(unifiedAssetService::findOne)
            .map(dto -> dto.getMetadata().getLabels().get(LabelConstants.LABEL_PRODUCT_VERSION))
            .map(Constants::formatVersionUsingV)
            .orElse(Constants.INIT_VERSION);
    if (StringUtils.isBlank(controlVersion)) {
      Optional.ofNullable(templateVersion)
          .ifPresent(
              any -> {
                systemInfoEntity.setControlProductVersion(templateVersion);
                systemInfoEntity.setStageProductVersion(templateVersion);
                systemInfoEntity.setProductionProductVersion(templateVersion);
                markFirstTemplateUpgraded(firstVersion.get());
              });
    } else {
      systemInfoEntity.setControlProductVersion(controlVersion);
      systemInfoEntity.setStageProductVersion(stageVersion);
      systemInfoEntity.setProductionProductVersion(productionVersion);
    }
  }

  private void markFirstTemplateUpgraded(String id) {
    UnifiedAssetEntity unifiedAssetEntity = unifiedAssetService.findOneByIdOrKey(id);
    unifiedAssetEntity.getLabels().put(LabelConstants.LABEL_FIRST_UPGRADE, "true");
    unifiedAssetRepository.save(unifiedAssetEntity);
  }

  @Transactional
  public void updateSystemStatus(SystemStateEnum systemStateEnum) {
    internalUpdateSystemStatus(systemStateEnum);
  }

  private void internalUpdateSystemStatus(SystemStateEnum systemStateEnum) {
    systemInfoRepository
        .findOneByKey(KEY)
        .ifPresent(
            entity -> {
              entity.setStatus(systemStateEnum.name());
              systemInfoRepository.save(entity);
            });
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSystemStatusImmediately(SystemStateEnum systemStateEnum) {
    internalUpdateSystemStatus(systemStateEnum);
  }

  @Transactional
  public void updateProductVersion(
      SystemStateEnum systemStateEnum,
      String controlVersion,
      String stageVersion,
      String productionVersion) {
    systemInfoRepository
        .findOneByKey(KEY)
        .ifPresent(
            entity -> {
              if (StringUtils.isNotBlank(controlVersion)) {
                entity.setControlProductVersion(controlVersion);
              }
              if (StringUtils.isNotBlank(stageVersion)) {
                entity.setStageProductVersion(stageVersion);
              }
              if (StringUtils.isNotBlank(productionVersion)) {
                entity.setProductionProductVersion(productionVersion);
              }
              if (systemStateEnum != null) {
                entity.setStatus(systemStateEnum.name());
              }
              systemInfoRepository.save(entity);
            });
  }

  @Transactional
  public void updateAppVersion(
      String controlVersion, String stageVersion, String productionVersion) {
    systemInfoRepository
        .findOneByKey(KEY)
        .ifPresent(
            entity -> {
              if (StringUtils.isNotBlank(controlVersion)) {
                entity.setControlAppVersion(controlVersion);
              }
              if (StringUtils.isNotBlank(stageVersion)) {
                entity.setStageAppVersion(stageVersion);
              }
              if (StringUtils.isNotBlank(productionVersion)) {
                entity.setProductionAppVersion(productionVersion);
              }
              systemInfoRepository.save(entity);
            });
  }

  public SystemInfo find() {
    return systemInfoRepository
        .findOneByKey(KEY)
        .map(SystemInfoMapper.INSTANCE::toDto)
        .map(
            t -> {
              t.setProductName(mgmtProperty.getProductName());
              return t;
            })
        .orElse(new SystemInfo());
  }
}
