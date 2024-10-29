package com.consoleconnect.kraken.operator.controller.api.v3;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.aspect.TemplateUpgradeBlockChecker;
import com.consoleconnect.kraken.operator.controller.dto.ComponentExpandDTO;
import com.consoleconnect.kraken.operator.controller.dto.CreateControlPlaneUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateProductionUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.dto.CreateUpgradeRequest;
import com.consoleconnect.kraken.operator.controller.event.TemplateUpgradeEvent;
import com.consoleconnect.kraken.operator.controller.service.TemplateUpgradeService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v3/products/{productId}/template-upgrade/",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Template Upgrade V3", description = "Template Upgrade V3")
@Slf4j
public class TemplateUpgradeV3Controller {
  private final TemplateUpgradeService templateUpgradeService;

  @Operation(summary = "control plane upgrade")
  @PostMapping("/control-plane")
  @TemplateUpgradeBlockChecker
  public Mono<HttpResponse<String>> controlPlaneUpgrade(
      @PathVariable("productId") String productId,
      @RequestBody CreateControlPlaneUpgradeRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                templateUpgradeService.controlPlaneUpgradeV3(
                    request.getTemplateUpgradeId(), userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "stage environment upgrade")
  @PostMapping("/stage")
  @TemplateUpgradeBlockChecker
  public Mono<HttpResponse<String>> stageUpgrade(
      @PathVariable("productId") String productId,
      @RequestBody CreateUpgradeRequest createUpgradeRequest) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId -> {
              TemplateUpgradeEvent templateUpgradeEvent = new TemplateUpgradeEvent();
              templateUpgradeEvent.setTemplateUpgradeId(
                  createUpgradeRequest.getTemplateUpgradeId());
              templateUpgradeEvent.setEnvId(createUpgradeRequest.getStageEnvId());
              templateUpgradeEvent.setUserId(userId);
              return templateUpgradeService.stageUpgradeV3(templateUpgradeEvent);
            })
        .map(HttpResponse::ok);
  }

  @Operation(summary = "product environment upgrade")
  @PostMapping("/production")
  @TemplateUpgradeBlockChecker
  public Mono<HttpResponse<String>> productUpgrade(
      @PathVariable("productId") String productId,
      @RequestBody CreateProductionUpgradeRequest createProductionUpgradeRequest) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId -> {
              templateUpgradeService.checkCondition2ProductionUpgrade(
                  createProductionUpgradeRequest);
              return templateUpgradeService.deployProductionV3(
                  createProductionUpgradeRequest.getTemplateUpgradeId(),
                  createProductionUpgradeRequest.getStageEnvId(),
                  createProductionUpgradeRequest.getProductEnvId(),
                  userId);
            })
        .map(HttpResponse::ok);
  }

  @Operation(summary = "list all api use case in a product that contained in the template")
  @GetMapping("/api-use-cases")
  public HttpResponse<List<ComponentExpandDTO>> listApiUseCases(
      @PathVariable String productId, @RequestParam String templateUpgradeId) {
    return HttpResponse.ok(templateUpgradeService.listApiUseCase(templateUpgradeId));
  }
}
