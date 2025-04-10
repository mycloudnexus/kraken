package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.controller.dto.UnifiedAssetDetailsDto;
import com.consoleconnect.kraken.operator.controller.mapper.AssetMapper;
import com.consoleconnect.kraken.operator.controller.service.ComponentAPIServerService;
import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.SearchQueryParams;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController("componentControllerV2")
@RequestMapping(
    value = "/v2/products/{productId}/components",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Component V2", description = "Component V2")
public class ComponentController {

  private final UnifiedAssetService service;
  private final ComponentAPIServerService componentAPIServerService;

  @Operation(summary = "List all components")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> searchComponents(
      @PathVariable("productId") String productId,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "false")
          boolean facetIncluded,
      @RequestParam(value = "parentProductType", required = false) String parentProductType,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    SearchQueryParams searchQueryParams =
        SearchQueryParams.builder()
            .parentId(productId)
            .kind(AssetKindEnum.COMPONENT_API.getKind())
            .facetIncluded(facetIncluded)
            .query(null)
            .parentProductType(parentProductType)
            .build();
    return HttpResponse.ok(
        service.search(searchQueryParams, getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Retrieve a component details")
  @GetMapping("/{componentId}")
  public HttpResponse<UnifiedAssetDto> findOneComponent(
      @PathVariable("productId") String productId,
      @PathVariable String componentId,
      @RequestParam(value = "linkIncluded", required = false, defaultValue = "false")
          boolean linkIncluded) {
    UnifiedAssetDto product = service.findOne(productId);
    UnifiedAssetDto asset = service.findOne(componentId);
    if (AssetKindEnum.COMPONENT_API.getKind().equalsIgnoreCase(asset.getKind())
        && product.getId().equalsIgnoreCase(asset.getParentId())) {
      UnifiedAssetDetailsDto details = AssetMapper.INSTANCE.toDetails(asset);
      if (linkIncluded) {
        List<AssetLinkDto> links =
            service.findAssetLinks(componentId, null, 0, Integer.MAX_VALUE).getData();
        details.setAssetLinks(links);
      }
      return HttpResponse.ok(details);
    } else if (AssetKindEnum.COMPONENT_API_TARGET_SPEC.getKind().equalsIgnoreCase(asset.getKind())
        && product.getId().equalsIgnoreCase(asset.getParentId())) {
      componentAPIServerService.inUse(asset);
      return HttpResponse.ok(asset);
    }
    throw KrakenException.badRequest("Asset is not a component");
  }
}
