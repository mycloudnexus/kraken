package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.aspect.TemplateUpgradeBlockChecker;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.service.ComponentTagService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.DeployStatusEnum;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/v2/products/{productId}/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product V2", description = "Product V2")
@Slf4j
public class ProductDeploymentController {
  private final ProductDeploymentService service;

  private final ComponentTagService componentTagService;

  @Operation(summary = "create a new deployment at component level")
  @PostMapping("deployments")
  public Mono<HttpResponse<UnifiedAssetDto>> create(
      @PathVariable String productId, @RequestBody CreateProductDeploymentRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                service.deployComponents(
                    productId, request, ReleaseKindEnum.COMPONENT_LEVEL, userId, false))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "create a new deployment to stage at the API mapper level")
  @PostMapping("api-mapper-deployments")
  @TemplateUpgradeBlockChecker
  public Mono<HttpResponse<UnifiedAssetDto>> createMapperDeployment(
      @PathVariable String productId, @RequestBody CreateAPIMapperDeploymentRequest request) {
    request.getMapperKeys().forEach(componentTagService::checkMapperModification);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                service.createMapperVersionAndDeploy(
                    productId, request, ReleaseKindEnum.API_LEVEL, userId, false))
        .map(
            item -> HttpResponse.of(HttpStatus.OK.value(), "Deployment request is accepted", item));
  }

  @Operation(summary = "create a deployment from stage to production at the API mapper level")
  @PostMapping("deploy-stage-to-production")
  @TemplateUpgradeBlockChecker
  public Mono<HttpResponse<UnifiedAssetDto>> deployStageToProduction(
      @PathVariable String productId, @RequestBody DeployToProductionRequest request) {
    Map<String, String> mapperKeyMap = componentTagService.checkProductionDeployRequest(request);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> service.deployStageToProduction(productId, request, userId, mapperKeyMap))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "verify a API mapper in labels")
  @PatchMapping("verify-api-mapper-in-labels")
  public Mono<HttpResponse<UnifiedAssetDto>> verifyMapperInLabels(
      @PathVariable String productId, @RequestBody VerifyMapperRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> service.verifyMapperInLabels(productId, request, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "list deployment at api mapper level")
  @GetMapping("api-mapper-deployments")
  public HttpResponse<Paging<ApiMapperDeploymentDTO>> retrieveApiMapperDeployment(
      @PathVariable String productId,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam(value = "mapperKey", required = false) String mapperKey,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        service.retrieveApiMapperDeployments(
            envId, mapperKey, null, getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "list running api mapper deployments in the env")
  @GetMapping("running-api-mapper-deployments")
  public HttpResponse<List<ApiMapperDeploymentDTO>> retrieveApiMapperDeployment(
      @PathVariable String productId, @RequestParam String envId) {
    Paging<ApiMapperDeploymentDTO> apiMapperDeploymentDTOPaging =
        service.retrieveApiMapperDeployments(
            envId,
            null,
            DeployStatusEnum.SUCCESS,
            PageRequest.of(0, 1000, Sort.Direction.DESC, AssetsConstants.FIELD_CREATE_AT));
    Set<String> inSet = new HashSet<>();
    List<ApiMapperDeploymentDTO> list =
        apiMapperDeploymentDTOPaging.getData().stream()
            .filter(t -> inSet.add(t.getTargetMapperKey()))
            .toList();
    return HttpResponse.ok(list);
  }

  @Operation(summary = "list latest running api mapper deployment information in the env")
  @GetMapping("latest-running-api-mapper-deployments")
  public HttpResponse<List<LatestDeploymentDTO>> queryLatestApiMapperDeployment(
      @PathVariable String productId, @RequestParam(value = "mapperKey") String mapperKey) {
    List<LatestDeploymentDTO> list = service.queryLatestApiMapperDeployment(productId, mapperKey);
    return HttpResponse.ok(list);
  }

  @Operation(summary = "search deployments")
  @GetMapping("deployments")
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @PathVariable String productId,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam(value = "componentId", required = false) String componentId,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "false")
          boolean facetIncluded,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info(
        "search,productId:{},envId:{},componentId:{},facetIncluded:{},q:{},orderBy:{},direction:{},page:{},size:{}",
        productId,
        envId,
        componentId,
        facetIncluded,
        q,
        orderBy,
        direction,
        page,
        size);
    return HttpResponse.ok(
        this.service.search(
            productId,
            envId,
            componentId,
            facetIncluded,
            q,
            getSearchPageRequest(page, size, direction, orderBy)));
  }
}
