package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.aspect.TemplateUpgradeBlockChecker;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.event.TemplateUpgradeEvent;
import com.consoleconnect.kraken.operator.controller.service.TemplateIngestService;
import com.consoleconnect.kraken.operator.controller.service.TemplateUpgradeService;
import com.consoleconnect.kraken.operator.core.dto.Tuple2;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.LabelConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/template-upgrade/",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Template Upgrade", description = "Template Upgrade")
@Slf4j
public class TemplateUpgradeController {
  private final UnifiedAssetService unifiedAssetService;
  private final TemplateIngestService templateIngestService;
  private final TemplateUpgradeService templateUpgradeService;
  private final AuthDataProperty.ResourceServer resourceServer;

  @Operation(summary = "list template upgrade releases change log")
  @GetMapping("/releases")
  public HttpResponse<Paging<TemplateUpgradeReleaseVO>> listTemplateChangeLog(
      @PathVariable("productId") String productId,
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
                AssetsConstants.FIELD_KIND, AssetKindEnum.PRODUCT_TEMPLATE_UPGRADE.getKind()),
            tuple3s,
            null,
            getSearchPageRequest(page, size, direction, orderBy),
            null);
    List<TemplateUpgradeReleaseVO> list =
        assetDtoPaging.getData().stream().map(this::toTemplateUpgradeReleaseVO).toList();
    // the latest can upgrade
    list.stream().findFirst().ifPresent(vo -> vo.setShowUpgradeButton(true));
    return HttpResponse.ok(
        PagingHelper.toPageNoSubList(
            list, assetDtoPaging.getPage(), assetDtoPaging.getSize(), assetDtoPaging.getTotal()));
  }

  protected TemplateUpgradeReleaseVO toTemplateUpgradeReleaseVO(UnifiedAssetDto assetDto) {
    TemplateUpgradeReleaseVO templateUpgradeReleaseVO = new TemplateUpgradeReleaseVO();
    Map<String, String> labels = assetDto.getMetadata().getLabels();
    templateUpgradeReleaseVO.setReleaseDate(labels.get(LabelConstants.LABEL_RELEASE_DATE));
    templateUpgradeReleaseVO.setReleaseVersion(labels.get(LabelConstants.LABEL_RELEASE_VERSION));
    templateUpgradeReleaseVO.setName(assetDto.getMetadata().getName());
    templateUpgradeReleaseVO.setDescription(assetDto.getMetadata().getDescription());
    templateUpgradeReleaseVO.setTemplateUpgradeId(assetDto.getId());
    Paging<TemplateUpgradeDeploymentVO> deploymentVOPaging =
        templateUpgradeService.listTemplateDeployment(
            templateUpgradeReleaseVO.getTemplateUpgradeId(), PageRequest.of(0, 10));
    templateUpgradeReleaseVO.setDeployments(deploymentVOPaging.getData());
    return templateUpgradeReleaseVO;
  }

  @Operation(summary = "list template upgrade deployments")
  @GetMapping("/template-deployments")
  public HttpResponse<Paging<TemplateUpgradeDeploymentVO>> listTemplateDeployment(
      @PathVariable("productId") String productId,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "templateUpgradeId", required = false) String templateUpgradeId,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {

    PageRequest pageRequest = getSearchPageRequest(page, size, direction, orderBy);
    return HttpResponse.ok(
        templateUpgradeService.listTemplateDeployment(templateUpgradeId, pageRequest));
  }

  @Operation(summary = "list of template upgrade details")
  @GetMapping("/template-deployments/{deploymentId}")
  public HttpResponse<List<MapperTagVO>> getDetail(
      @PathVariable("productId") String productId,
      @PathVariable("deploymentId") String deploymentId) {
    return HttpResponse.ok(templateUpgradeService.templateDeploymentDetails(deploymentId));
  }

  @Operation(summary = "show current upgrade version")
  @GetMapping("/current-versions")
  public HttpResponse<List<TemplateUpgradeDeploymentVO>> currentUpgradeVersion(
      @PathVariable("productId") String productId) {
    return HttpResponse.ok(templateUpgradeService.currentUpgradeVersion());
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
              templateUpgradeService.checkCondition2StageUpgrade(
                  createUpgradeRequest.getTemplateUpgradeId(),
                  createUpgradeRequest.getStageEnvId());
              TemplateUpgradeEvent templateUpgradeEvent = new TemplateUpgradeEvent();
              templateUpgradeEvent.setTemplateUpgradeId(
                  createUpgradeRequest.getTemplateUpgradeId());
              templateUpgradeEvent.setEnvId(createUpgradeRequest.getStageEnvId());
              templateUpgradeEvent.setUserId(userId);
              return templateIngestService.triggerManualTemplateUpgrade(templateUpgradeEvent);
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
              return templateUpgradeService.deployProduction(
                  createProductionUpgradeRequest.getTemplateUpgradeId(),
                  createProductionUpgradeRequest.getStageEnvId(),
                  createProductionUpgradeRequest.getProductEnvId(),
                  userId);
            })
        .map(HttpResponse::ok);
  }

  @Operation(summary = "whether update is allowed ")
  @GetMapping("/allow-update-operations")
  public HttpResponse<Boolean> allowUpdateOperations(@PathVariable("productId") String productId) {
    Paging<UnifiedAssetDto> assetDtoPaging =
        unifiedAssetService.findBySpecification(
            Tuple2.ofList(
                AssetsConstants.FIELD_KIND,
                AssetKindEnum.PRODUCT_TEMPLATE_DEPLOYMENT.getKind(),
                AssetsConstants.FIELD_STATUS,
                DeployStatusEnum.IN_PROCESS.name()),
            null,
            null,
            null,
            null);
    return HttpResponse.ok(CollectionUtils.isEmpty(assetDtoPaging.getData()));
  }
}
