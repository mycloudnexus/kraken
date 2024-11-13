package com.consoleconnect.kraken.operator.controller.service.upgrade;

import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeRecord;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.model.TemplateUpgradeDeploymentFacets;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.controller.service.SystemInfoService;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.enums.UpgradeResultEventEnum;
import com.consoleconnect.kraken.operator.core.event.TemplateUpgradeResultEvent;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;

@AllArgsConstructor
@Slf4j
public abstract class AbstractUpgradeSourceService implements UpgradeSourceService {
  @Getter private final AppProperty appProperty;
  @Getter private final ResourceLoaderFactory resourceLoaderFactory;
  private final UnifiedAssetRepository unifiedAssetRepository;
  private final MgmtProperty mgmtProperty;
  private final ApiComponentService apiComponentService;
  private final UnifiedAssetService unifiedAssetService;
  private final SystemInfoService systemInfoService;
  private final EventSinkService eventSinkService;
  protected static final List<String> CACHED_ASSET_KINDS =
      List.of(
          AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind(),
          AssetKindEnum.COMPONENT_API.getKind());

  public UnifiedAsset readFromPath(String fullPath) {
    Optional<FileContentDescriptor> fileContentDescriptor =
        resourceLoaderFactory.readFile(fullPath);
    if (fileContentDescriptor.isEmpty()) {
      log.warn("Unified Asset Not Found,Path: {}", fullPath);
      return null;
    }
    return YamlToolkit.parseYaml(fileContentDescriptor.get().getContent(), UnifiedAsset.class)
        .orElse(null);
  }

  public UpgradeTuple genProductUpgradeRecord(UnifiedAsset productAsset) {
    ProductFacets productFacets = UnifiedAsset.getFacets(productAsset, ProductFacets.class);
    // component
    List<String> componentPaths = productFacets.getComponentPaths();
    if (CollectionUtils.isEmpty(componentPaths)) {
      return null;
    }
    List<UpgradeRecord> templateUpgradeRecords =
        componentPaths.stream()
            .filter(path -> !appProperty.getInitializeExcludeAssets().contains(path))
            .map(
                componentPath -> {
                  UnifiedAsset unifiedAsset = readFromPath(componentPath);
                  if (unifiedAsset == null) {
                    return null;
                  }
                  return new UpgradeRecord(
                      unifiedAsset.getMetadata().getKey(),
                      unifiedAsset.getKind(),
                      unifiedAsset.getMetadata().getVersion(),
                      componentPath);
                })
            .filter(Objects::nonNull)
            .toList();

    List<String> componentKeys = templateUpgradeRecords.stream().map(UpgradeRecord::key).toList();
    Map<String, UnifiedAssetEntity> dbComponents =
        unifiedAssetRepository.findAllByKeyIn(componentKeys).stream()
            .collect(Collectors.toMap(UnifiedAssetEntity::getKey, t -> t));
    List<String> distributeKinds = mgmtProperty.getTemplateUpgrade().getDistributeKinds();
    List<UpgradeRecord> newVersionComponents = new ArrayList<>();
    List<UpgradeRecord> directSaves = new ArrayList<>();
    for (UpgradeRecord templateUpgradeRecord : templateUpgradeRecords) {
      if (!distributeKinds.contains(templateUpgradeRecord.kind())) {
        directSaves.add(templateUpgradeRecord);
      } else {
        if (!dbComponents.containsKey(templateUpgradeRecord.key())
            || templateUpgradeRecord.version()
                > dbComponents.get(templateUpgradeRecord.key()).getVersion()) {
          newVersionComponents.add(templateUpgradeRecord);
        }
      }
    }
    List<String> enforceUpgrades =
        productFacets.getTemplateUpgradePaths() == null
            ? List.of()
            : productFacets.getTemplateUpgradePaths();
    List<UpgradeRecord> upgradeComponents =
        enforceUpgrades.stream()
            .map(
                upgradePath -> {
                  UnifiedAsset unifiedAsset = readFromPath(upgradePath);
                  if (unifiedAsset == null) {
                    return null;
                  }
                  return new UpgradeRecord(
                      unifiedAsset.getMetadata().getKey(),
                      unifiedAsset.getKind(),
                      unifiedAsset.getMetadata().getVersion(),
                      upgradePath);
                })
            .filter(Objects::nonNull)
            .toList();

    return new UpgradeTuple(
        newVersionComponents, upgradeComponents, directSaves, productAsset.getMetadata().getKey());
  }

  protected UnifiedAssetDto fromUnifiedAsset(UnifiedAsset unifiedAsset) {
    UnifiedAssetDto unifiedAssetDto = new UnifiedAssetDto();
    BeanUtils.copyProperties(unifiedAsset, unifiedAssetDto);
    return unifiedAssetDto;
  }

  protected List<ComponentExpandDTO> convertFromSource(List<UnifiedAssetDto> allAssets) {
    Map<String, List<UnifiedAssetDto>> assetMap =
        allAssets.stream()
            .filter(t -> t.getKind() != null)
            .collect(Collectors.groupingBy(UnifiedAsset::getKind, Collectors.toList()));
    List<UnifiedAssetDto> componentList =
        assetMap.get(AssetKindEnum.COMPONENT_API.getKind()).stream()
            .filter(t -> t.getLinks() != null)
            .toList();
    return apiComponentService.convert(
        appProperty.getQueryExcludeAssetKeys(),
        componentList,
        assetMap.get(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind()));
  }

  @Override
  public void reportResult(String templateUpgradeId, String templateDeploymentId) {
    UnifiedAssetDto unifiedAssetDto = unifiedAssetService.findOne(templateUpgradeId);
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind()),
            Tuple2.ofList(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID, templateUpgradeId),
            null,
            null,
            null);
    List<UnifiedAssetDto> list = assetDtoPaging.getData();
    if (CollectionUtils.isEmpty(list)) {
      return;
    }
    if (StringUtils.isNotBlank(templateDeploymentId)) {
      UnifiedAssetDto templateDeployment = unifiedAssetService.findOne(templateDeploymentId);
      Optional.ofNullable(templateDeployment).ifPresent(dto -> report(dto, unifiedAssetDto));
    }
  }

  private void report(UnifiedAssetDto deploymentDto, UnifiedAssetDto templateDto) {
    boolean isStage =
        deploymentDto
            .getMetadata()
            .getLabels()
            .getOrDefault(LabelConstants.LABEL_ENV_NAME, "")
            .equalsIgnoreCase(EnvNameEnum.STAGE.name());
    EnvNameEnum envName = isStage ? EnvNameEnum.STAGE : EnvNameEnum.PRODUCTION;
    SystemInfo systemInfo = systemInfoService.find();
    String productVersion =
        templateDto.getMetadata().getLabels().get(LabelConstants.LABEL_PRODUCT_VERSION);
    if (DeployStatusEnum.SUCCESS.name().equalsIgnoreCase(deploymentDto.getMetadata().getStatus())) {
      TemplateUpgradeDeploymentFacets facets =
          UnifiedAsset.getFacets(deploymentDto, TemplateUpgradeDeploymentFacets.class);
      TemplateUpgradeDeploymentFacets.EnvDeployment envDeployment = facets.getEnvDeployment();

      if (CollectionUtils.isEmpty(envDeployment.getSystemDeployments())
          && CollectionUtils.isEmpty(envDeployment.getMapperDeployment())) {
        eventSinkService.reportTemplateUpgradeResult(
            templateDto,
            UpgradeResultEventEnum.UPGRADE,
            event -> {
              event.setEnvName(envName);
              event.setUpgradeBeginAt(ZonedDateTime.now());
              event.setUpgradeEndAt(ZonedDateTime.now().plusSeconds(5L));
              fillProductInfo(systemInfo, productVersion, event);
            });
      } else {
        eventSinkService.reportTemplateUpgradeResult(
            templateDto,
            UpgradeResultEventEnum.UPGRADE,
            event -> {
              event.setEnvName(envName);
              event.setUpgradeEndAt(ZonedDateTime.now().plusSeconds(5L));
              fillProductInfo(systemInfo, productVersion, event);
            });
      }

    } else {
      eventSinkService.reportTemplateUpgradeResult(
          templateDto,
          UpgradeResultEventEnum.UPGRADE,
          event -> {
            event.setEnvName(envName);
            event.setUpgradeBeginAt(ZonedDateTime.now());
            fillProductInfo(systemInfo, productVersion, event);
          });
    }
  }

  private void fillProductInfo(
      SystemInfo systemInfo, String productVersion, TemplateUpgradeResultEvent event) {
    event.setProductKey(systemInfo.getProductKey());
    event.setProductSpec(systemInfo.getProductSpec());
    event.setProductVersion(productVersion);
  }
}
