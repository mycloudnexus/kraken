package com.consoleconnect.kraken.operator.controller.service;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetLinkKindEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.model.AssetLink;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UnifiedAssetCache {
  public static final String KEY_MAPPER_COMPONENT = "MAPPER_COMPONENT";
  public static final String KEY_MAPPER_ASSET = "MAPPER_ASSET";
  private final LoadingCache<String, Map<String, Pair<String, String>>> mapperToComponentCache;
  private final LoadingCache<String, Map<String, UnifiedAssetDto>> mapperAssetCache;
  private final UnifiedAssetService unifiedAssetService;

  public UnifiedAssetCache(UnifiedAssetService unifiedAssetService) {
    this.unifiedAssetService = unifiedAssetService;
    CacheLoader<String, Map<String, Pair<String, String>>> loader;
    loader =
        new CacheLoader<>() {
          @Override
          public Map<String, Pair<String, String>> load(String key) {
            return internalGetMapper2Component();
          }
        };
    mapperToComponentCache =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(300)).build(loader);
    mapperAssetCache =
        CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(60))
            .build(
                new CacheLoader<>() {
                  @Override
                  public Map<String, UnifiedAssetDto> load(String key) {
                    return internalGetMapperAssetMap();
                  }
                });
  }

  @SneakyThrows
  public Map<String, Pair<String, String>> getMapper2Component() {
    return mapperToComponentCache.get(KEY_MAPPER_COMPONENT);
  }

  @SneakyThrows
  public Map<String, UnifiedAssetDto> getMapperAssetMap() {
    return mapperAssetCache.get(KEY_MAPPER_ASSET);
  }

  private Map<String, Pair<String, String>> internalGetMapper2Component() {
    log.info("internalGetMapper2Component");
    // <mapperKey,<componentKey,componentName>>
    Map<String, Pair<String, String>> mapper2Component = new HashMap<>();
    unifiedAssetService
        .search(null, AssetKindEnum.COMPONENT_API.getKind(), true, null, PageRequest.of(0, 100))
        .getData()
        .forEach(
            comAsset ->
                comAsset.getLinks().stream()
                    .filter(this::isMapperOrWorkflow)
                    .forEach(
                        link ->
                            mapper2Component.put(
                                link.getTargetAssetKey(),
                                Pair.of(
                                    comAsset.getMetadata().getKey(),
                                    comAsset.getMetadata().getName()))));
    return mapper2Component;
  }

  private boolean isMapperOrWorkflow(AssetLink link) {
    String relationship = link.getRelationship();
    return relationship.equalsIgnoreCase(AssetLinkKindEnum.IMPLEMENTATION_TARGET_MAPPER.getKind())
        || relationship.equalsIgnoreCase(AssetLinkKindEnum.IMPLEMENTATION_WORKFLOW.getKind());
  }

  public Map<String, UnifiedAssetDto> internalGetMapperAssetMap() {
    log.info("internalGetMapperAssetMap");
    return unifiedAssetService
        .search(
            null,
            AssetKindEnum.COMPONENT_API_TARGET_MAPPER.getKind(),
            true,
            null,
            PageRequest.of(0, 100))
        .getData()
        .stream()
        .collect(Collectors.toMap(t -> t.getMetadata().getKey(), t -> t));
  }

  @EventListener(PlatformSettingCompletedEvent.class)
  @Async
  public void initialize() {
    mapperToComponentCache.refresh(KEY_MAPPER_COMPONENT);
    mapperAssetCache.refresh(KEY_MAPPER_ASSET);
  }
}
