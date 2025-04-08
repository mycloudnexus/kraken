package com.consoleconnect.kraken.operator.core.controller;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.SearchQueryParams;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
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

@ConditionalOnExpression("'${app.unified-asset.endpoints.exposure.include}'.contains('component')")
@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/products/{productId}/components",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unified Model Mgmt", description = "Unified Model Mgmt APIs")
public class ComponentController {

  private final UnifiedAssetService service;

  @Operation(summary = "search components")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @PathVariable("productId") String productId,
      @RequestParam(value = "kind", required = false) String kind,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "true")
          boolean facetIncluded,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "parentProductType", required = false) String parentProductType,
      @RequestParam(value = "productType", required = false) String productType,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    String productUuid = this.service.findOne(productId).getId();
    SearchQueryParams searchQueryParams =
        SearchQueryParams.builder()
            .parentId(productUuid)
            .kind(kind)
            .facetIncluded(facetIncluded)
            .query(q)
            .parentProductType(parentProductType)
            .build();
    return HttpResponse.ok(
        this.service.search(
            searchQueryParams, getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Retrieve a component by id")
  @GetMapping("/{id}")
  public HttpResponse<UnifiedAssetDto> findOne(
      @PathVariable("productId") String productId, @PathVariable("id") String id) {
    service.findOne(productId);
    return HttpResponse.ok(service.findOne(id));
  }

  @Operation(summary = "Retrieve a asset by id")
  @GetMapping("/{id}/links")
  public HttpResponse<Paging<AssetLinkDto>> findAssetLinks(
      @PathVariable("productId") String productId,
      @PathVariable("id") String id,
      @RequestParam(value = "relationship", required = false) String relationship,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    service.findOne(productId);
    return HttpResponse.ok(service.findAssetLinks(id, relationship, page, size));
  }
}
