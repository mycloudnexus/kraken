package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPIServerRequest;
import com.consoleconnect.kraken.operator.controller.service.ComponentAPIServerService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/components/{componentId}/api-servers",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Component V2", description = "Component V2")
@Slf4j
public class ComponentAPIServerController {
  private final ComponentAPIServerService service;

  @Operation(summary = "create/update a api-server for the  api component")
  @PostMapping()
  public Mono<HttpResponse<String>> createAPIServer(
      @PathVariable String productId,
      @PathVariable String componentId,
      @RequestBody CreateAPIServerRequest request) {
    log.info(
        "createAPIServer,productId:{},componentId:{},request:{}", productId, componentId, request);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                service.createAPIServer(componentId, request, userId).getData().getId().toString())
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Delete API server")
  @DeleteMapping()
  public Mono<HttpResponse<Boolean>> removeAPIServer(
      @PathVariable String productId, @PathVariable String componentId) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> service.deleteAPIServer(productId, componentId, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "list api servers of an api component")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @PathVariable String productId,
      @PathVariable String componentId,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "false")
          boolean facetIncluded,
      @RequestParam(value = "liteSearch", required = false, defaultValue = "false")
          boolean liteSearch,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info(
        "listAPIServers,productId:{},componentId:{},facetIncluded:{},orderBy:{},direction:{},page:{},size:{}",
        productId,
        componentId,
        facetIncluded,
        orderBy,
        direction,
        page,
        size);
    return HttpResponse.ok(
        this.service.listAPIServers(
            componentId,
            facetIncluded,
            liteSearch,
            null,
            getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "list api servers of an api component")
  @GetMapping("/{name}")
  public HttpResponse<Boolean> checkServerAPIName(
      @PathVariable String productId, @PathVariable String componentId, @PathVariable String name) {
    boolean exist = this.service.checkServerAPIName(productId, componentId, name);
    if (exist) {
      String err = "The API Server name has existed:" + name;
      return HttpResponse.of(HttpStatus.BAD_REQUEST.value(), err, Boolean.FALSE);
    }
    return HttpResponse.ok(true);
  }
}
