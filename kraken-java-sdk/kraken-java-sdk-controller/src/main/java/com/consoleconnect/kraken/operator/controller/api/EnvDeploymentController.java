package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.ComponentVersionDto;
import com.consoleconnect.kraken.operator.controller.dto.CreateProductDeploymentRequest;
import com.consoleconnect.kraken.operator.controller.dto.EnvironmentComponentDto;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.enums.ReleaseKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product Deployment Mgmt ", description = "Product Deployment Mgmt")
@Slf4j
public class EnvDeploymentController {

  private final ProductDeploymentService productDeploymentService;

  @Operation(summary = "retrieve deployed components corresponding to a release")
  @GetMapping("/deployments")
  public HttpResponse<Paging<EnvironmentComponentDto>> retrieveDeployedComponents(
      @PathVariable("productId") String productId,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "history", required = false, defaultValue = "false") boolean history,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    PageRequest pageRequest = UnifiedAssetService.getSearchPageRequest(page, size);
    return HttpResponse.ok(
        productDeploymentService.retrieveDeployedComponents(envId, status, history, pageRequest));
  }

  @Operation(summary = "retrieve running components corresponding to a environment")
  @GetMapping("/running-components")
  public HttpResponse<Paging<EnvironmentComponentDto>> retrieveRunningComponents(
      @PathVariable("productId") String productId,
      @RequestParam(value = "envId", required = false) String envId) {
    return HttpResponse.ok(productDeploymentService.queryRunningComponent(envId));
  }

  @Operation(summary = "deploy specific components into the environment")
  @PostMapping("/envs/{envId}/deployment")
  public Mono<HttpResponse<Object>> buildAndDeployComponents(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestBody List<ComponentVersionDto> components) {
    if (productId.equalsIgnoreCase("undefined")) {
      productId = "mef.sonata";
    }
    final String finalProductId = productId;
    CreateProductDeploymentRequest createProductDeploymentRequest =
        new CreateProductDeploymentRequest();
    createProductDeploymentRequest.setEnvId(envId);
    createProductDeploymentRequest.setTagIds(
        components.stream().map(ComponentVersionDto::getId).toList());

    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                productDeploymentService.deployComponents(
                    finalProductId,
                    createProductDeploymentRequest,
                    ReleaseKindEnum.COMPONENT_LEVEL,
                    userId,
                    false))
        .map(HttpResponse::ok);
  }
}
