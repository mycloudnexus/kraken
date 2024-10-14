package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController("productControllerV2")
@RequestMapping(value = "/v2/products", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product V2", description = "Product V2")
public class ProductController {

  private final UnifiedAssetService service;

  @Operation(summary = "List all products")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "false")
          boolean facetIncluded,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        service.search(
            null,
            AssetKindEnum.PRODUCT.getKind(),
            facetIncluded,
            null,
            getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Find a product details")
  @GetMapping("/{productId}")
  public HttpResponse<UnifiedAssetDto> findOne(@PathVariable("productId") String productId) {
    UnifiedAssetDto asset = service.findOne(productId);
    if (AssetKindEnum.PRODUCT.getKind().equalsIgnoreCase(asset.getKind())) {
      return HttpResponse.ok(asset);
    }
    throw KrakenException.badRequest("Asset is not a product");
  }
}
