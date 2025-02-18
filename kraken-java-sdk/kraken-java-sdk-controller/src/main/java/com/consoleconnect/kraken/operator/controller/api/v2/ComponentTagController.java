package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateTagRequest;
import com.consoleconnect.kraken.operator.controller.service.ComponentTagService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/components/{componentId}/tags",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Component V2", description = "Component V2")
@Slf4j
public class ComponentTagController {
  private final ComponentTagService componentTagService;

  @Operation(summary = "create a new tag for the  api component")
  @PostMapping()
  public Mono<HttpResponse<String>> createTag(
      @PathVariable String productId,
      @PathVariable String componentId,
      @RequestBody CreateTagRequest request) {
    log.info("createTag,productId:{},componentId:{},request:{}", productId, componentId, request);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                componentTagService
                    .createTag(componentId, request, userId)
                    .getData()
                    .getId()
                    .toString())
        .map(HttpResponse::ok);
  }

  @Operation(summary = "list tags of an api component")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @PathVariable String productId,
      @PathVariable String componentId,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "false")
          boolean facetIncluded,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    log.info(
        "listTags,productId:{},componentId:{},facetIncluded:{},orderBy:{},direction:{},page:{},size:{}",
        productId,
        componentId,
        facetIncluded,
        orderBy,
        direction,
        page,
        size);
    return HttpResponse.ok(
        this.componentTagService.search(
            componentId,
            facetIncluded,
            null,
            getSearchPageRequest(page, size, direction, orderBy)));
  }
}
