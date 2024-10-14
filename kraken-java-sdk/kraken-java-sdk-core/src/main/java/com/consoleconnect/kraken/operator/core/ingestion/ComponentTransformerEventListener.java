package com.consoleconnect.kraken.operator.core.ingestion;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.FileContentDescriptor;
import com.consoleconnect.kraken.operator.core.model.FileDescriptor;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentTransformerFacets;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ComponentTransformerEventListener extends AbstractAssetEventListener {

  public ComponentTransformerEventListener(
      ApplicationEventPublisher eventPublisher, AppProperty appProperty) {
    super(eventPublisher, appProperty);
  }

  public AssetKindEnum getKind() {
    return AssetKindEnum.COMPONENT_TRANSFORMER;
  }

  @Override
  public void onPrePersist(
      FileDescriptor fileDescriptor, UnifiedAsset asset, DataIngestionJob job) {
    log.info(
        "onPrePersist, assetKind:{},assetKey:{}", asset.getKind(), asset.getMetadata().getKey());
    ComponentTransformerFacets facets =
        UnifiedAsset.getFacets(asset, ComponentTransformerFacets.class);

    ComponentTransformerFacets.Script script = facets.getScript();

    if (script == null) {
      log.warn("Script is null for asset: {}", asset.getMetadata().getKey());
      return;
    }
    if (script.getCode() != null) {
      log.info("Script code is not null for asset: {}", asset.getMetadata().getKey());
      return;
    }
    if (script.getPath() == null) {
      log.warn("Script path is null for asset: {}", asset.getMetadata().getKey());
      return;
    }

    Optional<FileContentDescriptor> fileContentDescriptorOptional =
        job.readFile(facets.getScript().getPath());
    if (fileContentDescriptorOptional.isPresent()) {
      FileContentDescriptor fileContentDescriptor = fileContentDescriptorOptional.get();
      script.setCode(
          Base64.getEncoder()
              .encodeToString(fileContentDescriptor.getContent().getBytes(StandardCharsets.UTF_8)));
      asset.getFacets().put(ComponentTransformerFacets.FACET_SCRIPT_KEY, script);
    }
  }
}
