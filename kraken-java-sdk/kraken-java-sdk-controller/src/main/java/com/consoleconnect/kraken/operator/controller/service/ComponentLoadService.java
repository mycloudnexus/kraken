package com.consoleconnect.kraken.operator.controller.service;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_TARGET_SPEC;

import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.ingestion.ResourceLoaderFactory;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.Metadata;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ProductFacets;
import com.consoleconnect.kraken.operator.core.repo.MgmtEventRepository;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@EnableRetry
public class ComponentLoadService {
  private final UnifiedAssetService unifiedAssetService;
  private final ResourceLoaderFactory loaderFactory;
  private final ApiComponentService apiComponentService;
  private final MgmtEventRepository eventRepository;
  private static final String DEFAULT_SERVER_NAME = "mock seller API";

  public ComponentLoadService(
      UnifiedAssetService unifiedAssetService,
      ResourceLoaderFactory loaderFactory,
      MgmtEventRepository eventRepository,
      ApiComponentService apiComponentService) {
    this.unifiedAssetService = unifiedAssetService;
    this.loaderFactory = loaderFactory;
    this.eventRepository = eventRepository;
    this.apiComponentService = apiComponentService;
  }

  @EventListener(value = MgmtEventEntity.class, condition = "#entity.eventType == 'RESET'")
  @Transactional
  public void onResetEvent(MgmtEventEntity entity) {
    log.info("reset event: {}", JsonToolkit.toJson(entity));
    // clear mapper data in control plane
    UnifiedAssetDto dto = unifiedAssetService.findOne(entity.getResourceId());
    if (dto == null) {
      throw KrakenException.notFound("can not find mapper to be reset");
    }
    clearMapperData(dto);
    eventRepository.save(entity);
  }

  private void clearMapperData(UnifiedAssetDto dto) {
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(dto, ComponentAPITargetFacets.class);
    ComponentAPITargetFacets.Endpoint endpoint = facets.getEndpoints().get(0);
    endpoint.setServerKey(StringUtils.EMPTY);
    endpoint.setPath(StringUtils.EMPTY);
    endpoint.setMethod(StringUtils.EMPTY);
    ComponentAPITargetFacets.Mappers mappers = endpoint.getMappers();
    if (mappers.getRequest() != null) {
      mappers.getRequest().removeIf(mapper -> Boolean.TRUE.equals(mapper.getCustomizedField()));
      mappers
          .getRequest()
          .forEach(
              mapper -> {
                mapper.setTarget(StringUtils.EMPTY);
                mapper.setTargetType(StringUtils.EMPTY);
                mapper.setTargetLocation(StringUtils.EMPTY);
                mapper.setTargetValues(Collections.emptyList());
              });
    }
    if (mappers.getResponse() != null) {
      mappers.getResponse().removeIf(mapper -> Boolean.TRUE.equals(mapper.getCustomizedField()));
      mappers
          .getResponse()
          .forEach(
              mapper -> {
                mapper.setSource(StringUtils.EMPTY);
                mapper.setSourceLocation(StringUtils.EMPTY);
                mapper.setValueMapping(Collections.emptyMap());
              });
    }
    dto.setFacets(JsonToolkit.fromJson(JsonToolkit.toJson(facets), Map.class));
    if (dto.getMetadata() == null) {
      dto.setMetadata(new Metadata());
    }
    if (dto.getMetadata().getLabels() == null) {
      dto.getMetadata().setLabels(new HashMap<>());
    }
    dto.getMetadata()
        .getLabels()
        .put(
            LabelConstants.LABEL_STAGE_DEPLOYED_STATUS,
            LabelConstants.VALUE_DEPLOYED_STATUS_NOT_DEPLOYED);
    apiComponentService.updateUnifiedAssetEntity(dto, dto.getId(), null);
  }

  public UnifiedAssetDto loadMapperData(String key) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(key);
    List<String> sampleMapperDataPath = getSampleMapperDataPath();

    if (sampleMapperDataPath == null) {
      throw KrakenException.notFound("can not find data by key");
    }
    sampleMapperDataPath.forEach(
        fullPath -> {
          Optional<FileContentDescriptor> yamlContentOptional = loaderFactory.readFile(fullPath);
          if (yamlContentOptional.isEmpty()) {
            return;
          }
          Optional<UnifiedAsset> assetOptional =
              YamlToolkit.parseYaml(yamlContentOptional.get().getContent(), UnifiedAsset.class);
          if (assetOptional.isEmpty()) {
            return;
          }
          if (Objects.equals(assetOptional.get().getMetadata().getKey(), key)) {
            setServerKey(assetOptional.get(), getServerKey());
            assetDto.setFacets(assetOptional.get().getFacets());
          }
        });
    return assetDto;
  }

  private void setServerKey(UnifiedAsset unifiedAsset, String serverKey) {
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(unifiedAsset, ComponentAPITargetFacets.class);
    facets.getEndpoints().get(0).setServerKey(serverKey);
    unifiedAsset.setFacets(JsonToolkit.fromJson(JsonToolkit.toJson(facets), Map.class));
  }

  private String getServerKey() {
    String serverKey = "";
    List<UnifiedAssetDto> assetDtos =
        unifiedAssetService.findByKind(COMPONENT_API_TARGET_SPEC.getKind());
    if (assetDtos == null) {
      return StringUtils.EMPTY;
    }
    Optional<UnifiedAssetDto> assetDtoOptional =
        assetDtos.stream()
            .filter(
                v ->
                    v.getMetadata() != null
                        && Objects.equals(DEFAULT_SERVER_NAME, v.getMetadata().getName()))
            .findAny();
    if (assetDtoOptional.isPresent()) {
      serverKey = assetDtoOptional.get().getMetadata().getKey();
    }
    return serverKey;
  }

  private List<String> getSampleMapperDataPath() {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            List.of(new Tuple2(AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT.getKind())),
            null,
            null,
            null,
            null);
    UnifiedAssetDto productAssets =
        assetDtoPaging.getData().get(assetDtoPaging.getData().size() - 1);
    ProductFacets facets = UnifiedAsset.getFacets(productAssets, ProductFacets.class);
    return facets.getSampleMapperDataPaths();
  }
}
