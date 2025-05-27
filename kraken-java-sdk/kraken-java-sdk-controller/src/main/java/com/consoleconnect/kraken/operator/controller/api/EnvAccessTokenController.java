package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateAPITokenRequest;
import com.consoleconnect.kraken.operator.controller.model.APIToken;
import com.consoleconnect.kraken.operator.controller.service.APITokenService;
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
@RequestMapping(value = "/products/{productId}/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "API Token Mgmt ", description = "API Token Mgmt")
public class EnvAccessTokenController {

  private final APITokenService apiTokenService;

  @Operation(summary = "List all access tokens")
  @GetMapping("/envs/{envId}/api-tokens")
  public HttpResponse<Paging<APIToken>> search(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "revoked", required = false, defaultValue = "false") boolean revoked,
      @RequestParam(value = "expiredIncluded", required = false, defaultValue = "false")
          boolean expiredIncluded,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        apiTokenService.search(
            productId, envId, revoked, expiredIncluded, PageRequest.of(page, size)));
  }

  @Operation(summary = "Retrieve a access token details")
  @GetMapping("/envs/{envId}/api-tokens/{tokenId}")
  public HttpResponse<APIToken> findOne(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @PathVariable String tokenId) {
    return HttpResponse.ok(apiTokenService.findOne(tokenId));
  }

  @Operation(summary = "Revoke a access token")
  @DeleteMapping("/envs/{envId}/api-tokens/{tokenId}")
  public Mono<HttpResponse<APIToken>> delete(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @PathVariable String tokenId) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> apiTokenService.revokeToken(tokenId, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Create a access token")
  @PostMapping("/envs/{envId}/api-tokens")
  public Mono<HttpResponse<APIToken>> create(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestBody CreateAPITokenRequest request) {
    request.setEnvId(envId);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> apiTokenService.createToken(productId, request, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "List all access tokens")
  @GetMapping("/env-api-tokens")
  public HttpResponse<Paging<APIToken>> listAll(
      @PathVariable("productId") String productId,
      @RequestParam(value = "revoked", required = false, defaultValue = "false") boolean revoked,
      @RequestParam(value = "expiredIncluded", required = false, defaultValue = "false")
          boolean expiredIncluded,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        apiTokenService.search(
            productId, null, revoked, expiredIncluded, PageRequest.of(page, size)));
  }

  @Operation(summary = "Rotate all api tokens under an environment")
  @PostMapping("/envs/{envId}/rotate-api-tokens")
  public Mono<HttpResponse<APIToken>> revokeAll(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestBody CreateAPITokenRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> apiTokenService.revokeAllTokenByEnvId(productId, request, userId, envId))
        .map(HttpResponse::ok);
  }
}
