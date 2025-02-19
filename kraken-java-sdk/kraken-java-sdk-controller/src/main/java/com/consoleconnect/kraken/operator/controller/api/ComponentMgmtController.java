package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.model.ComponentTagFacet;
import com.consoleconnect.kraken.operator.controller.service.ApiComponentService;
import com.consoleconnect.kraken.operator.controller.service.ComponentTagService;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.annotation.AuditAction;
import com.consoleconnect.kraken.operator.core.annotation.Auditable;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Component Management ", description = "component management")
@Auditable
public class ComponentMgmtController {
  private final ApiComponentService apiComponentService;
  private final ComponentTagService componentTagService;
  private final ProductDeploymentService productDeploymentService;

  @Operation(summary = "generate a new version for the  api component")
  @PostMapping("/components/{componentId}/versions")
  public Mono<HttpResponse<String>> createVersion(
      @RequestBody CreateTagRequest createTagRequest,
      @PathVariable String componentId,
      @PathVariable String productId) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                componentTagService
                    .createTag(componentId, createTagRequest, userId)
                    .getData()
                    .getId()
                    .toString())
        .map(HttpResponse::ok);
  }

  @Operation(summary = "list version of an api component")
  @GetMapping("/components/{componentId}/versions")
  public HttpResponse<Paging<ComponentVersionDto>> listVersions(
      @PathVariable String componentId,
      @RequestParam(required = false) String componentKey,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    PageRequest pageRequest =
        UnifiedAssetService.getSearchPageRequest(page, size, direction, orderBy);
    Paging<UnifiedAssetDto> assetDtoPaging =
        componentTagService.search(componentId, true, null, pageRequest);
    List<ComponentVersionDto> componentVersionDtos =
        assetDtoPaging.getData().stream()
            .map(
                dto -> {
                  ComponentVersionDto componentVersionDto = new ComponentVersionDto();
                  componentVersionDto.setVersion(
                      dto.getMetadata().getLabels().get(LabelConstants.LABEL_VERSION_NAME));
                  ComponentTagFacet componentTagFacet =
                      UnifiedAsset.getFacets(dto, new TypeReference<>() {});
                  List<UnifiedAssetDto> list = Lists.newArrayList();
                  list.add(componentTagFacet.getComponent());
                  list.addAll(componentTagFacet.getChildren());
                  componentVersionDto.setAssetDtoList(list);
                  return componentVersionDto;
                })
            .toList();
    return HttpResponse.ok(
        PagingHelper.toPage(
            componentVersionDtos, assetDtoPaging.getPage(), assetDtoPaging.getSize()));
  }

  @Operation(
      summary = "list running versions of an api component deployed in different environments")
  @GetMapping("/components/{componentKey}/running-versions")
  public HttpResponse<List<ComponentVersionWithEnvDto>> listVersionsWithEnv(
      @PathVariable String componentKey, @PathVariable String productId) {
    return HttpResponse.ok(productDeploymentService.listComponentVersionWithEnv(componentKey));
  }

  @Operation(summary = "list versions of all components)")
  @GetMapping("/component-versions")
  public HttpResponse<List<ComponentWitheVersionDTO>> listVersions(@PathVariable String productId) {
    return HttpResponse.ok(componentTagService.listVersions());
  }

  @Operation(summary = "update target mapper)")
  @PatchMapping("/components/{id}/targetMapper")
  @AuditAction(
      resource = AuditConstants.API_MAPPING,
      resourceId = "#pathVariable['id']",
      description = "update target api mapper")
  public Mono<IngestionDataResult> updateMapper(
      @PathVariable String productId, @PathVariable String id, @RequestBody UnifiedAsset asset) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> apiComponentService.updateApiTargetMapper(asset, id, userId));
  }

  @Operation(summary = "The detail of mapping for an api component")
  @GetMapping("/components/{componentId}/mapper-details")
  public HttpResponse<ComponentExpandDTO> detailMapping(
      @PathVariable String productId,
      @PathVariable String componentId,
      @RequestParam(value = "envId", required = false) String envId) {
    return HttpResponse.ok(
        apiComponentService.queryComponentExpandInfo(productId, componentId, envId));
  }

  @Operation(summary = "list all api use case in a product")
  @GetMapping("/api-use-cases")
  public HttpResponse<List<ComponentExpandDTO>> listAllApiUseCases(@PathVariable String productId) {
    return HttpResponse.ok(apiComponentService.listAllApiUseCase());
  }

  @Operation(summary = "The usage detail of component API Server spec")
  @GetMapping("/components/{componentId}/spec-details")
  public HttpResponse<EndPointUsageDTO> detailSpec(
      @PathVariable String productId, @PathVariable String componentId) {
    return HttpResponse.ok(apiComponentService.queryEndPointUsageDetail(productId, componentId));
  }

  @Operation(summary = "list all supported components and product categories")
  @GetMapping("/product-categories")
  public HttpResponse<ComponentProductCategoryDTO> listProductCategories(
      @PathVariable String productId) {
    return HttpResponse.ok(apiComponentService.listProductCategories(productId));
  }
}
