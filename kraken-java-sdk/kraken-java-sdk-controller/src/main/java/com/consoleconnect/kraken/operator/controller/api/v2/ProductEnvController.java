package com.consoleconnect.kraken.operator.controller.api.v2;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.service.ProductEnvironmentService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/environments",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product V2", description = "Product V2")
public class ProductEnvController {

  private final ProductEnvironmentService environmentService;

  @Operation(summary = "List a product's environments")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @PathVariable("productId") String productId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(environmentService.search(productId, PageRequest.of(page, size)));
  }

  @Operation(summary = "Create a env")
  @PostMapping()
  public Mono<HttpResponse<UnifiedAssetDto>> create(
      @PathVariable("productId") String productId, @RequestBody CreateEnvRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> environmentService.create(productId, request, userId))
        .map(HttpResponse::ok);
  }
}
