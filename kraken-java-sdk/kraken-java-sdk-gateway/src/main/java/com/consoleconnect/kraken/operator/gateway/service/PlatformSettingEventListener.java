package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingCompletedEvent;
import com.consoleconnect.kraken.operator.core.ingestion.DataIngestionJob;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentTransformerFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@Slf4j
public class PlatformSettingEventListener {
  private final JavaScriptEngine javaScriptEngine;
  private final ApplicationEventPublisher publisher;
  private final UnifiedAssetService unifiedAssetService;
  private final DataIngestionJob dataIngestionJob;

  @EventListener(PlatformSettingCompletedEvent.class)
  public void handlePlatformSettingCompletedEvent(PlatformSettingCompletedEvent event) {

    log.info("PlatformSettingCompletedEvent received");
    // upload source code of component transformer to JavaScriptEngine
    unifiedAssetService
        .search(
            null,
            AssetKindEnum.COMPONENT_TRANSFORMER.getKind(),
            true,
            null,
            PageRequest.of(0, 1000))
        .getData()
        .forEach(
            asset -> {
              ComponentTransformerFacets componentTransformerFacets =
                  JsonToolkit.fromJson(
                      JsonToolkit.toJson(asset.getFacets()), ComponentTransformerFacets.class);
              ComponentTransformerFacets.Script script = componentTransformerFacets.getScript();
              if (script != null && script.getCode() != null) {
                log.info(
                    "Adding source code into javascript engine: {}", asset.getMetadata().getKey());
                String decodedCode = new String(Base64.decode(script.getCode()));
                javaScriptEngine.addSource(asset.getMetadata().getKey(), decodedCode);
              }
            });

    // notify gateway to refresh routes
    log.info("PlatformSettingCompletedEvent publishing RefreshRoutesEvent");
    publisher.publishEvent(new RefreshRoutesEvent(this));
    log.info("PlatformSettingCompletedEvent completed");
  }

  @EventListener(classes = ApplicationReadyEvent.class)
  public void onPlatformBootUp(Object event) {
    log.info("Platform Boot Up Event Received, event class:{}", event.getClass());
    dataIngestionJob.ingestionWorkspace();
    publisher.publishEvent(new PlatformSettingCompletedEvent());
    log.info("Platform Boot Up Event Completed");
  }
}
