package com.consoleconnect.kraken.operator.controller.service.upgrade;

import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.controller.service.SystemInfoService;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.*;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceCacheHolder;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ProductReleaseDownloadFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import com.consoleconnect.kraken.operator.core.service.EventSinkService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.fasterxml.jackson.core.type.TypeReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MgmtSourceUpgradeService extends AbstractUpgradeSourceService {

  private final UnifiedAssetService unifiedAssetService;
  private final ResourceCacheHolder resourceCacheHolder;
  private final Map<String, WeakReference<List<UnifiedAssetDto>>> cache = new HashMap<>();

  @Autowired
  public MgmtSourceUpgradeService(
      AppProperty appProperty,
      ResourceLoaderFactory resourceLoaderFactory,
      UnifiedAssetRepository unifiedAssetRepository,
      MgmtProperty mgmtProperty,
      UnifiedAssetService unifiedAssetService,
      EventSinkService eventSinkService,
      SystemInfoService systemInfoService,
      ResourceCacheHolder resourceCacheHolder,
      ApiComponentService apiComponentService) {
    super(
        appProperty,
        resourceLoaderFactory,
        unifiedAssetRepository,
        mgmtProperty,
        apiComponentService,
        unifiedAssetService,
        systemInfoService,
        eventSinkService);
    this.unifiedAssetService = unifiedAssetService;
    this.resourceCacheHolder = resourceCacheHolder;
  }

  @Override
  public List<UpgradeTuple> getTemplateUpgradeRecords(String templateUpgradeId) {
    Paging<UnifiedAssetDto> downloadAssetPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_PARENT_ID,
                templateUpgradeId,
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_RELEASE_DOWNLOAD.getKind()),
            null,
            null,
            null,
            null);
    UnifiedAssetDto downloadAsset = downloadAssetPaging.getData().get(0);
    ProductReleaseDownloadFacets downloadFacets =
        UnifiedAsset.getFacets(downloadAsset, ProductReleaseDownloadFacets.class);
    UnifiedAsset productAsset = resourceCacheHolder.findSourceProduct(downloadFacets);
    return Stream.of(productAsset)
        .map(t -> this.appendTemplateId(templateUpgradeId, productAsset))
        .map(this::genProductUpgradeRecord)
        .filter(Objects::nonNull)
        .toList();
  }

  private UnifiedAsset appendTemplateId(String templateId, UnifiedAsset unifiedAssetDto) {
    ProductFacets productFacets = UnifiedAsset.getFacets(unifiedAssetDto, ProductFacets.class);
    List<String> componentPaths = formatFullPath(templateId, productFacets.getComponentPaths());
    List<String> templateUpgradePaths =
        formatFullPath(templateId, productFacets.getTemplateUpgradePaths());
    List<String> sampleConfigPaths =
        formatFullPath(templateId, productFacets.getSampleConfigPaths());
    List<String> sampleMapperDataPaths =
        formatFullPath(templateId, productFacets.getSampleMapperDataPaths());
    productFacets.setComponentPaths(componentPaths);
    productFacets.setTemplateUpgradePaths(templateUpgradePaths);
    productFacets.setSampleConfigPaths(sampleConfigPaths);
    productFacets.setSampleMapperDataPaths(sampleMapperDataPaths);
    Map<String, Object> facetsMap =
        JsonToolkit.fromJson(productFacets, new TypeReference<Map<String, Object>>() {});
    unifiedAssetDto.setFacets(facetsMap);
    return unifiedAssetDto;
  }

  private List<String> formatFullPath(String templateId, List<String> paths) {
    return Optional.ofNullable(paths).stream()
        .flatMap(List::stream)
        .map(t -> resourceCacheHolder.appendTemplateIdToFullPath(templateId, t))
        .toList();
  }

  @Override
  public String supportedUpgradeSource() {
    return UpgradeSourceEnum.MGMT.name();
  }

  @Override
  public List<ComponentExpandDTO> listApiUseCases(String templateId) {
    List<UnifiedAssetDto> assetDtos =
        Optional.ofNullable(cache.get(templateId)).map(WeakReference::get).orElse(null);
    if (assetDtos == null) {
      UnifiedAssetDto downloadAsset =
          unifiedAssetService
              .findBySpecification(
                  Tuple2.ofList(
                      AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_RELEASE_DOWNLOAD.getKind()),
                  Tuple2.ofList(LabelConstants.LABEL_APP_TEMPLATE_UPGRADE_ID, templateId),
                  null,
                  null,
                  null)
              .getData()
              .get(0);
      ProductReleaseDownloadFacets downloadFacets =
          UnifiedAsset.getFacets(downloadAsset, ProductReleaseDownloadFacets.class);
      assetDtos =
          downloadFacets.getContentMap().values().stream()
              .map(t -> YamlToolkit.parseYaml(t, UnifiedAsset.class))
              .map(t -> t.orElse(null))
              .filter(Objects::nonNull)
              .filter(t -> t.getKind() != null)
              .filter(t -> CACHED_ASSET_KINDS.contains(t.getKind()))
              .map(this::fromUnifiedAsset)
              .toList();
      cache.put(templateId, new WeakReference<>(assetDtos));
    }
    return convertFromSource(assetDtos);
  }
}
