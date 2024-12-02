package com.consoleconnect.kraken.operator.controller.api;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_CREATE_AT_ORIGINAL;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.BuyerAssetDto;
import com.consoleconnect.kraken.operator.controller.dto.CreateBuyerRequest;
import com.consoleconnect.kraken.operator.controller.model.MgmtProperty;
import com.consoleconnect.kraken.operator.controller.service.BuyerService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/products/{productId}/buyers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Buyer Information Management", description = "Buyer Information Management")
public class BuyerMgmtController {

  private BuyerService buyerService;

  @Operation(summary = "Create a buyer")
  @PostMapping("")
  public Mono<HttpResponse<BuyerAssetDto>> create(
      @PathVariable("productId") String productId, @RequestBody CreateBuyerRequest buyerOnBoard) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> buyerService.create(productId, buyerOnBoard, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Regenerate a new token for a buyer")
  @PostMapping("/{id}/access-tokens")
  @Validated
  public Mono<HttpResponse<BuyerAssetDto>> regenerate(
      @Valid @NotBlank @PathVariable("productId") String productId,
      @Valid @NotBlank @PathVariable("id") String id,
      @RequestParam(
              value = "tokenExpiredInSeconds",
              required = false,
              defaultValue = MgmtProperty.DEFAULT_TOKEN_EXPIRED_SECONDS)
          Long tokenExpiredInSeconds) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> buyerService.regenerate(productId, id, tokenExpiredInSeconds, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "List buyers")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @PathVariable("productId") String productId,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam(value = "buyerId", required = false) String buyerId,
      @RequestParam(value = "status", required = false) String status,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        buyerService.search(
            productId,
            envId,
            buyerId,
            status,
            orderBy,
            PageRequest.of(page, size, direction, FIELD_CREATE_AT_ORIGINAL)));
  }

  @Operation(summary = "activate buyer")
  @PostMapping(value = "/{id}/activate")
  public Mono<HttpResponse<BuyerAssetDto>> activate(
      @PathVariable String productId, @PathVariable("id") String id) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> buyerService.activate(productId, id, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "deactivate buyer")
  @PostMapping(value = "/{id}/deactivate")
  public Mono<HttpResponse<Boolean>> deactivate(
      @PathVariable String productId, @PathVariable("id") String id) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> buyerService.deactivate(productId, id, userId))
        .map(HttpResponse::ok);
  }
}
