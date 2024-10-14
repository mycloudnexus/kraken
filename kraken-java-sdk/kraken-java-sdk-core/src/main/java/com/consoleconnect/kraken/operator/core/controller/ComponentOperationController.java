package com.consoleconnect.kraken.operator.core.controller;

import com.consoleconnect.kraken.operator.core.annotation.AuditAction;
import com.consoleconnect.kraken.operator.core.annotation.Auditable;
import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import com.consoleconnect.kraken.operator.core.enums.ResourceLoaderTypeEnum;
import com.consoleconnect.kraken.operator.core.event.IngestDataEvent;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.DataIngestionService;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AuditConstants;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.concurrent.CompletableFuture;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@ConditionalOnExpression(
    "'${app.unified-asset.endpoints.exposure.include}'.contains('component-operation')")
@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/products/{productId}/components",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unified Model Mgmt", description = "Unified Model Mgmt APIs")
@Auditable
public class ComponentOperationController {

  private final UnifiedAssetService service;
  private final AppProperty appProperty;
  private final DataIngestionService dataIngestionService;

  @Operation(summary = "Add a component")
  @PostMapping()
  @SneakyThrows
  @AuditAction(
      resource = AuditConstants.TARGET_SPEC,
      resourceId = "#responseBody['data']['data']['id']",
      conditionOn = "#requestBody['kind']=='kraken.component.api-target-spec'",
      description = "create target api server")
  public HttpResponse<IngestionDataResult> create(
      @PathVariable("productId") String productId, @RequestBody UnifiedAsset asset) {
    if (!appProperty
        .getTenant()
        .getComponentOperation()
        .getCreatableAssetKinds()
        .contains(asset.getKind())) {
      throw KrakenException.forbidden("Asset kind is not allowed to create");
    }
    String productUuid = this.service.findOne(productId).getId();
    IngestDataEvent event = new IngestDataEvent();
    event.setParentId(productUuid);
    event.setFullPath(
        ResourceLoaderTypeEnum.generatePath(ResourceLoaderTypeEnum.RAW, JsonToolkit.toJson(asset)));
    CompletableFuture<IngestionDataResult> ingestionDataResultCompletableFuture =
        dataIngestionService.ingestData(event);
    IngestionDataResult ingestionDataResult = ingestionDataResultCompletableFuture.get();
    if (ingestionDataResult.getCode() != HttpStatus.OK.value()) {
      throw KrakenException.internalError(ingestionDataResult.getMessage());
    }
    return HttpResponse.ok(ingestionDataResult);
  }

  @Operation(summary = "Update a component by id")
  @PatchMapping("/{id}")
  @SneakyThrows
  @AuditAction(
      resource = AuditConstants.TARGET_SPEC,
      resourceId = "#pathVariable['id']",
      conditionOn = "#requestBody['kind']=='kraken.component.api-target-spec'",
      description = "update target api server")
  public HttpResponse<IngestionDataResult> updateOne(
      @PathVariable("productId") String productId,
      @PathVariable("id") String id,
      @RequestBody UnifiedAsset asset) {
    if (!appProperty
        .getTenant()
        .getComponentOperation()
        .getUpdatableAssetKinds()
        .contains(asset.getKind())) {
      throw KrakenException.forbidden("Component is not allowed to update");
    }
    UnifiedAssetEntity entity = service.findOneByIdOrKey(id);
    IngestDataEvent event = new IngestDataEvent();
    event.setAsset(asset);
    event.setParentId(entity.getParentId());
    event.setFullPath("raw:" + JsonToolkit.toJson(asset));
    CompletableFuture<IngestionDataResult> ingestionDataResultCompletableFuture =
        dataIngestionService.ingestData(event);
    IngestionDataResult ingestionDataResult = ingestionDataResultCompletableFuture.get();
    if (ingestionDataResult.getCode() != HttpStatus.OK.value()) {
      throw KrakenException.internalError(ingestionDataResult.getMessage());
    }
    return HttpResponse.ok(ingestionDataResult);
  }
}
