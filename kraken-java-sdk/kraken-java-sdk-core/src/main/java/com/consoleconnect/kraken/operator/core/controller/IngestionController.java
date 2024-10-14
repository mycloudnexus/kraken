package com.consoleconnect.kraken.operator.core.controller;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.event.PlatformSettingChangedEvent;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@ConditionalOnExpression("'${app.unified-asset.endpoints.exposure.include}'.contains('ingestion')")
@AllArgsConstructor
@RestController()
@RequestMapping(value = "/ingestion", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unified Model Mgmt", description = "Unified Model Mgmt APIs")
public class IngestionController {

  private final ApplicationEventPublisher eventPublisher;

  @Operation(summary = "Reload configuration")
  @PostMapping("/reload")
  public HttpResponse<UnifiedAssetDto> getDetail() {
    eventPublisher.publishEvent(new PlatformSettingChangedEvent());
    return HttpResponse.ok(null);
  }
}
