package com.consoleconnect.kraken.operator.core.controller;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@ConditionalOnExpression("'${app.unified-asset.endpoints.exposure.include}'.contains('product')")
@AllArgsConstructor
@RestController()
@RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unified Model Mgmt", description = "Unified Model Mgmt APIs")
public class ProductController {

  private final UnifiedAssetService service;

  @Operation(summary = "search products")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "true")
          boolean facetIncluded,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "parentProductType", required = false) String parentProductType,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        this.service.search(
            null,
            AssetKindEnum.PRODUCT.getKind(),
            facetIncluded,
            q,
            getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Get product detail")
  @GetMapping("/{productId}")
  public HttpResponse<UnifiedAssetDto> getDetail(@PathVariable("productId") String productId) {
    var result = this.service.findOne(productId);
    return HttpResponse.ok(result);
  }
}
