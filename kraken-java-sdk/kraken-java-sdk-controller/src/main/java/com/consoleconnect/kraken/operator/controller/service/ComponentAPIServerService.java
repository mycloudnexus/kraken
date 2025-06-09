package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_SPEC;

import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.dto.APIServerEnvDTO;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetStatusEnum;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@Getter
@AllArgsConstructor
@Service
@Slf4j
public class ComponentAPIServerService extends AssetStatusManager {
  private static final String ENVIRONMENTS_KEY = "environments";
  private static final String BASE_SPEC_KEY = "baseSpec";
  private static final String SELECTED_API_KEY = "selectedAPIs";
  private static final String CONTENT = "content";
  private final UnifiedAssetService unifiedAssetService;

  @Transactional
  public IngestionDataResult createAPIServer(
      String componentId, CreateAPIServerRequest request, String createdBy) {

    // create a snapshot of the component
    // current component
    UnifiedAssetDto unifiedAssetEntity = unifiedAssetService.findOne(componentId);

    String key =
        request.getKey() != null
            ? request.getKey()
            : unifiedAssetEntity.getMetadata().getKey()
                + ".api-server."
                + System.currentTimeMillis();
    UnifiedAsset tagAsset =
        UnifiedAsset.of(COMPONENT_API_TARGET_SPEC.getKind(), key, request.getName());
    tagAsset.getMetadata().setDescription(request.getDescription());
    tagAsset.getMetadata().setStatus(AssetStatusEnum.ACTIVATED.getKind());
    Map<String, Object> facets = new HashMap<>();
    facets.put(SELECTED_API_KEY, request.getSelectedAPIs());
    facets.put(ENVIRONMENTS_KEY, request.getEnvironments());
    tagAsset.setFacets(facets);
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    return unifiedAssetService.syncAsset(
        unifiedAssetEntity.getMetadata().getKey(), tagAsset, syncMetadata, true);
  }

  public Paging<UnifiedAssetDto> listAPIServers(
      String componentId,
      boolean facetIncluded,
      boolean liteSearch,
      String q,
      PageRequest pageRequest) {
    Paging<UnifiedAssetDto> pages =
        this.unifiedAssetService.search(
            componentId, COMPONENT_API_TARGET_SPEC.getKind(), facetIncluded, q, pageRequest);
    filterFacet(facetIncluded, liteSearch, pages);
    Set<String> serverKeyUsage = queryServerKeyInUsage();
    pages
        .getData()
        .forEach(
            assetDto -> {
              String specKey = assetDto.getMetadata().getKey();
              assetDto.setInUse((serverKeyUsage.contains(specKey) ? Boolean.TRUE : Boolean.FALSE));
            });
    return pages;
  }

  public void filterFacet(
      boolean facetIncluded, boolean liteSearch, Paging<UnifiedAssetDto> pages) {
    if (!facetIncluded || !liteSearch || pages.getSize() == 0) {
      return;
    }
    pages
        .getData()
        .forEach(
            assetDto -> {
              assetDto.setSyncMetadata(null);
              Map<String, Object> facets = assetDto.getFacets();
              if (MapUtils.isEmpty(facets) || Objects.isNull(facets.get(ENVIRONMENTS_KEY))) {
                return;
              }
              Object env = facets.get(ENVIRONMENTS_KEY);
              assetDto.setFacets(new HashMap<>());
              assetDto.getFacets().put(ENVIRONMENTS_KEY, env);

              Object baseSpec = facets.get(BASE_SPEC_KEY);
              if (baseSpec != null) {
                Map<String, Object> map = JsonToolkit.fromJson(baseSpec, Map.class);
                map.remove(CONTENT);
                assetDto.getFacets().put(BASE_SPEC_KEY, map);
              }
            });
  }

  public void inUse(UnifiedAssetDto assetDto) {
    Set<String> serverKeyUsage = queryServerKeyInUsage();
    String specKey = assetDto.getMetadata().getKey();
    assetDto.setInUse((serverKeyUsage.contains(specKey) ? Boolean.TRUE : Boolean.FALSE));
  }

  @Transactional
  public List<APIServerEnvDTO> buildAPIServerCache() {
    Paging<UnifiedAssetDto> paging =
        unifiedAssetService.search(
            null,
            COMPONENT_API_TARGET_SPEC.getKind(),
            true,
            null,
            UnifiedAssetService.getSearchPageRequest(0, 1000));
    return paging.getData().stream()
        .flatMap(
            dto -> {
              // <name,url>
              Map<String, String> apiServerMap =
                  JsonToolkit.fromJson(
                      JsonToolkit.toJson(dto.getFacets().get("environments")),
                      new TypeReference<Map<String, String>>() {});

              return apiServerMap.entrySet().stream()
                  .map(
                      entry -> {
                        APIServerEnvDTO apiServerEnvDTO = new APIServerEnvDTO();
                        apiServerEnvDTO.setApiServerKey(dto.getMetadata().getKey());
                        apiServerEnvDTO.setUrl(entry.getValue());
                        apiServerEnvDTO.setEnvName(entry.getKey());
                        return apiServerEnvDTO;
                      });
            })
        .toList();
  }

  public Set<String> queryServerKeyInUsage() {
    return unifiedAssetService
        .findByKind(AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind())
        .stream()
        .map(
            assetDto -> {
              ComponentAPITargetFacets facets =
                  UnifiedAsset.getFacets(assetDto, ComponentAPITargetFacets.class);
              return facets.getEndpoints().get(0).getServerKey();
            })
        .collect(Collectors.toSet());
  }

  public Boolean deleteAPIServer(String productId, String id, String deletedBy) {
    log.info("API server asset:{} is deleted by:{}, productId:{}", id, deletedBy, productId);
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(id);
    Assert.state(
        COMPONENT_API_TARGET_SPEC.getKind().equals(assetDto.getKind()),
        "Asset kind deleted should be " + COMPONENT_API_TARGET_SPEC.getKind());
    unifiedAssetService.deleteOne(id);
    return true;
  }

  public Boolean checkServerAPIName(String productId, String componentId, String name) {
    log.info(
        "API server asset name checking,productId:{}, componentId:{}, server name:{}",
        productId,
        componentId,
        name);
    return unifiedAssetService
        .search(
            productId,
            COMPONENT_API_TARGET_SPEC.getKind(),
            false,
            null,
            UnifiedAssetService.getSearchPageRequest(0, 1000))
        .getData()
        .stream()
        .anyMatch(asset -> asset.getMetadata().getName().equals(name));
  }
}
