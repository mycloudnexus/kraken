package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateEnvRequest;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.service.EnvironmentService;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
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
@RequestMapping(value = "/products/{productId}/envs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Env Mgmt ", description = "Env Mgmt")
public class EnvController {

  private final EnvironmentService environmentService;
  private final AppProperty appProperty;

  @Operation(summary = "List a product's environments")
  @GetMapping()
  public HttpResponse<Paging<Environment>> search(
      @PathVariable("productId") String productId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(environmentService.search(productId, PageRequest.of(page, size)));
  }

  @Operation(summary = "Retrieve a env details")
  @GetMapping("/{envId}")
  public HttpResponse<Environment> findOne(
      @PathVariable("productId") String productId, @PathVariable("envId") String envId) {
    return HttpResponse.ok(environmentService.findOne(envId));
  }

  @Operation(summary = "Create a env")
  @PostMapping()
  public Mono<HttpResponse<Environment>> create(
      @PathVariable("productId") String productId, @RequestBody CreateEnvRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> environmentService.create(productId, request, userId))
        .map(HttpResponse::ok);
  }
}
