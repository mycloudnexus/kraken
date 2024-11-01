package com.consoleconnect.kraken.operator.controller.api.v3;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.aspect.TemplateUpgradeBlockChecker;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.event.TemplateUpgradeEvent;
import com.consoleconnect.kraken.operator.controller.service.TemplateUpgradeService;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.Sort;
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
  public static final String UPGRADED = "Upgraded";
  public static final String NOT_UPGRADED = "Not upgraded";
  public static final String UPGRADING = "Upgrading";
  public static final String DEPRECATED = "Deprecated";
  private final TemplateUpgradeService templateUpgradeService;
  private final UnifiedAssetService unifiedAssetService;

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

  @Operation(summary = "list template upgrade releases change log")
  @GetMapping("/releases")
  public HttpResponse<Paging<TemplateUpgradeReleaseVO>> listTemplateChangeLog(
      @PathVariable("productId") String productId,
      @RequestParam(value = "templateUpgradeId", required = false) String templateUpgradeId,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    List<Tuple2> tuple3s = List.of();
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind(),
                AssetsConstants.FIELD_ID,
                templateUpgradeId),
            tuple3s,
            null,
            getSearchPageRequest(page, size, direction, orderBy),
            null);
    List<TemplateUpgradeReleaseVO> list =
        assetDtoPaging.getData().stream()
            .map(templateUpgradeService::generateTemplateUpgradeReleaseVO)
            .toList();
    // the latest can upgrade
    list.stream().findFirst().ifPresent(vo -> this.calculateStatus(vo, true));
    list.stream().skip(1).forEach(vo -> this.calculateStatus(vo, false));
    return HttpResponse.ok(
        PagingHelper.toPageNoSubList(
            list, assetDtoPaging.getPage(), assetDtoPaging.getSize(), assetDtoPaging.getTotal()));
  }

  protected void calculateStatus(TemplateUpgradeReleaseVO vo, boolean first) {
    boolean deployedProduction =
        vo.getDeployments().stream()
            .anyMatch(
                deploy -> deploy.getEnvName().equalsIgnoreCase(EnvNameEnum.PRODUCTION.name()));
    if (deployedProduction) {
      vo.setStatus(UPGRADED);
      return;
    }
    if (first) {
      if (CollectionUtils.isEmpty(vo.getDeployments())) {
        vo.setStatus(NOT_UPGRADED);
        return;
      }
      vo.setStatus(UPGRADING);
    } else {
      vo.setStatus(DEPRECATED);
    }
  }

  @Operation(summary = "stage environment upgrade check")
  @GetMapping("/stage-upgrade-check")
  public HttpResponse<List<String>> checkStageUpgradeCondition(
      @PathVariable("productId") String productId,
      @RequestParam(value = "templateUpgradeId", required = false) String templateUpgradeId,
      @RequestParam String stageEnvId) {
    return HttpResponse.ok(templateUpgradeService.stageCheck(templateUpgradeId, stageEnvId));
  }

  @Operation(summary = "product environment upgrade check")
  @GetMapping("/production-upgrade-check")
  public HttpResponse<List<String>> checkProductionUpgradeCondition(
      @PathVariable("productId") String productId,
      @RequestParam(value = "templateUpgradeId", required = false) String templateUpgradeId,
      @RequestParam String productionEnvId) {
    return HttpResponse.ok(
        templateUpgradeService.productionCheck(templateUpgradeId, productionEnvId));
  }
}
