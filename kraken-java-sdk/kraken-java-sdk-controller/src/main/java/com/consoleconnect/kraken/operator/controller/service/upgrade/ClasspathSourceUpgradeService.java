package com.consoleconnect.kraken.operator.controller.service.upgrade;

import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.dto.UpgradeTuple;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.UpgradeSourceEnum;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import com.consoleconnect.kraken.operator.core.model.facet.WorkspaceFacets;
import com.consoleconnect.kraken.operator.core.repo.UnifiedAssetRepository;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClasspathSourceUpgradeService extends AbstractUpgradeSourceService {
  private WeakReference<List<UnifiedAssetDto>> cache;

  @Autowired
  public ClasspathSourceUpgradeService(
      AppProperty appProperty,
      ResourceLoaderFactory resourceLoaderFactory,
      UnifiedAssetRepository unifiedAssetRepository,
      MgmtProperty mgmtProperty,
      ApiComponentService apiComponentService) {
    super(
        appProperty,
        resourceLoaderFactory,
        unifiedAssetRepository,
        mgmtProperty,
        apiComponentService);
  }

  @Override
  public List<UpgradeTuple> getTemplateUpgradeRecords(String templateUpgradeId) {
    return getWorkspaceFacets().getProductPaths().stream()
        .map(this::readFromPath)
        .map(this::genProductUpgradeRecord)
        .filter(Objects::nonNull)
        .toList();
  }

  @Override
  public String supportedUpgradeSource() {
    return UpgradeSourceEnum.CLASSPATH.name();
  }

  public WorkspaceFacets getWorkspaceFacets() {
    String fullPath = getAppProperty().getTenant().getWorkspacePath();
    UnifiedAsset workplace = readFromPath(fullPath);
    return UnifiedAsset.getFacets(workplace, WorkspaceFacets.class);
  }

  public UnifiedAsset getProductAsset() {
    WorkspaceFacets workspaceFacets = getWorkspaceFacets();
    return workspaceFacets.getProductPaths().stream()
        .filter(Objects::nonNull)
        .findFirst()
        .map(this::readFromPath)
        .orElse(null);
  }

  @Override
  public List<ComponentExpandDTO> listApiUseCases(String templateId) {
    UnifiedAsset productAsset = this.getProductAsset();
    ProductFacets productFacets = UnifiedAsset.getFacets(productAsset, ProductFacets.class);
    List<UnifiedAssetDto> assetDtos =
        Optional.ofNullable(cache).map(WeakReference::get).orElse(null);
    if (assetDtos == null) {
      assetDtos =
          productFacets.getComponentPaths().stream()
              .map(this::readFromPath)
              .filter(Objects::nonNull)
              .filter(t -> CACHED_ASSET_KINDS.contains(t.getKind()))
              .map(this::fromUnifiedAsset)
              .toList();
      cache = new WeakReference<>(assetDtos);
    }
    return convertFromSource(assetDtos);
  }
}
