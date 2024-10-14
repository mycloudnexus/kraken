package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_SPEC;

import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.dto.APIServerEnvDTO;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.*;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
@Slf4j
public class ComponentAPIServerService {
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

    Map<String, Object> facets = new HashMap<>();
    facets.put("selectedAPIs", request.getSelectedAPIs());
    facets.put("environments", request.getEnvironments());
    tagAsset.setFacets(facets);
    SyncMetadata syncMetadata = new SyncMetadata("", "", DateTime.nowInUTCString(), createdBy);
    return unifiedAssetService.syncAsset(
        unifiedAssetEntity.getMetadata().getKey(), tagAsset, syncMetadata, true);
  }

  public Paging<UnifiedAssetDto> listAPIServers(
      String componentId, boolean facetIncluded, String q, PageRequest pageRequest) {
    return this.unifiedAssetService.search(
        componentId, COMPONENT_API_TARGET_SPEC.getKind(), facetIncluded, q, pageRequest);
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
}
