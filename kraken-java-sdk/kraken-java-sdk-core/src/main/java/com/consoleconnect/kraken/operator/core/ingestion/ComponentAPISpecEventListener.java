package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ComponentAPISpecEventListener extends AbstractAssetEventListener {

  public ComponentAPISpecEventListener(
      ApplicationEventPublisher eventPublisher, AppProperty appProperty) {
    super(eventPublisher, appProperty);
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_API_SPEC;
  }

  @Override
  public void onPrePersist(
      FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    log.info(
        "onPrePersist, assetKind:{},assetKey:{}", asset.getKind(), asset.getMetadata().getKey());
    ComponentAPISpecFacets facets = UnifiedAsset.getFacets(asset, ComponentAPISpecFacets.class);

    loadAPISpecFromPath(facets.getBaseSpec(), asset.getMetadata().getKey(), job)
        .ifPresent(
            content -> {
              facets.getBaseSpec().setContent(content);
              asset
                  .getFacets()
                  .put(ComponentAPISpecFacets.FACET_API_BASE_SPEC, facets.getBaseSpec());
            });
    loadAPISpecFromPath(facets.getCustomizedSpec(), asset.getMetadata().getKey(), job)
        .ifPresent(
            content -> {
              facets.getCustomizedSpec().setContent(content);
              asset
                  .getFacets()
                  .put(
                      ComponentAPISpecFacets.FACET_API_CUSTOMIZED_SPEC, facets.getCustomizedSpec());
            });
  }

  private Optional<String> loadAPISpecFromPath(
      ComponentAPISpecFacets.APISpec apiSpec, String assetKey, DataIngestionJob job) {
    log.info("Reading apiDoc content for asset: {}", assetKey);
    if (apiSpec == null) {
      log.warn("APIDoc is null for asset: {}", assetKey);
      return Optional.empty();
    }
    if (apiSpec.getContent() != null) {
      log.info("APIDoc content is not null for asset: {}", assetKey);
      return Optional.empty();
    }
    if (apiSpec.getPath() == null) {
      log.warn("APIDoc path is null for asset: {}", assetKey);
      return Optional.empty();
    }

    Optional<FileContentDescriptor> fileContentDescriptorOptional = job.readFile(apiSpec.getPath());
    if (fileContentDescriptorOptional.isPresent()) {
      FileContentDescriptor fileContentDescriptor = fileContentDescriptorOptional.get();
      String content =
          Base64.getEncoder()
              .encodeToString(fileContentDescriptor.getContent().getBytes(StandardCharsets.UTF_8));
      return Optional.of(content);
    }
    return Optional.empty();
  }
}
