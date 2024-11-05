package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.controller.entity.SystemInfoEntity;
import com.consoleconnect.kraken.operator.controller.enums.SystemStateEnum;
import com.consoleconnect.kraken.operator.controller.mapper.SystemInfoMapper;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.repo.SystemInfoRepository;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Constants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import java.util.List;
import java.util.Optional;
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

  @Value("${spring.build.version}")
  private String buildVersion;

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
    String productSpec =
        Optional.ofNullable(list.get(0))
            .map(UnifiedAssetDto::getMetadata)
            .map(Metadata::getLabels)
            .map(t -> t.get(LabelConstants.MEF_API_RELEASE))
            .orElse(null);
    String version =
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
            .orElse(Constants.INIT_VERSION);
    systemInfoRepository
        .findOneByKey(KEY)
        .or(
            () -> {
              SystemInfoEntity systemInfoEntity = new SystemInfoEntity();
              systemInfoEntity.setKey(KEY);
              systemInfoEntity.setStatus(SystemStateEnum.RUNNING.name());
              return Optional.of(systemInfoEntity);
            })
        .ifPresent(
            systemInfoEntity -> {
              systemInfoEntity.setControlAppVersion(buildVersion);
              systemInfoEntity.setProductKey(productKey);
              systemInfoEntity.setProductSpec(productSpec);
              systemInfoEntity.setControlProductVersion(version);
              systemInfoRepository.save(systemInfoEntity);
            });
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
        .orElseThrow(() -> KrakenException.notFound("System info not found"));
  }
}
