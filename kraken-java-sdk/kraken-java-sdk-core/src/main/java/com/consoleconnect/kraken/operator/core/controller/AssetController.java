package com.consoleconnect.kraken.operator.core.controller;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.core.dto.AssetLinkDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.APISpecEnum;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPISpecFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Base64;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@ConditionalOnExpression("'${app.unified-asset.endpoints.exposure.include}'.contains('asset')")
@AllArgsConstructor
@RestController()
@RequestMapping(value = "/assets", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unified Asset Model Mgmt", description = "Unified Asset Model Mgmt APIs")
public class AssetController {

  private final UnifiedAssetService service;

  @Operation(summary = "search assets")
  @GetMapping()
  public HttpResponse<Paging<UnifiedAssetDto>> search(
      @RequestParam(value = "parentId", required = false) String parentId,
      @RequestParam(value = "kind", required = false) String kind,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "true")
          boolean facetIncluded,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    if (parentId != null) {
      parentId = this.service.findOneByIdOrKey(parentId).getId().toString();
    }
    return HttpResponse.ok(
        this.service.search(
            parentId,
            kind,
            facetIncluded,
            q,
            getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Retrieve a asset by id")
  @GetMapping("/{id}")
  public HttpResponse<UnifiedAssetDto> findOne(@PathVariable("id") String id) {
    return HttpResponse.ok(service.findOne(id));
  }

  @Operation(summary = "Retrieve a asset's children by id")
  @GetMapping("/{id}/children")
  public HttpResponse<Paging<UnifiedAssetDto>> findAssetChildren(
      @PathVariable("id") String id,
      @RequestParam(value = "kind", required = false) String kind,
      @RequestParam(value = "facetIncluded", required = false, defaultValue = "true")
          boolean facetIncluded,
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        service.search(
            id, kind, facetIncluded, q, getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Retrieve a asset's links by id")
  @GetMapping("/{id}/links")
  public HttpResponse<Paging<AssetLinkDto>> findAssetLinks(
      @PathVariable("id") String id,
      @RequestParam(value = "relationship", required = false) String relationship,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(service.findAssetLinks(id, relationship, page, size));
  }

  @GetMapping(value = "/{id}/api-docs", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public byte[] downloadAPIDoc(
      @PathVariable("id") String id,
      @RequestParam(value = "specType", required = false) APISpecEnum specType) {

    UnifiedAssetDto component = service.findOne(id);
    if (!AssetKindEnum.COMPONENT_API_SPEC.getKind().equalsIgnoreCase(component.getKind())) {
      throw KrakenException.badRequest("Asset is not a component API spec");
    }
    ComponentAPISpecFacets componentAPISpecFacets =
        UnifiedAsset.getFacets(component, ComponentAPISpecFacets.class);
    if (componentAPISpecFacets == null) {
      throw KrakenException.badRequest("Component does not have API docs");
    }

    if (specType == null || specType == APISpecEnum.CUSTOMIZED) {
      if (componentAPISpecFacets.getCustomizedSpec() == null
          || componentAPISpecFacets.getCustomizedSpec().getContent() == null) {
        throw KrakenException.badRequest("Component does not have customized API docs");
      }
      return Base64.getDecoder().decode(componentAPISpecFacets.getCustomizedSpec().getContent());
    }
    if (componentAPISpecFacets.getBaseSpec() == null
        || componentAPISpecFacets.getBaseSpec().getContent() == null) {
      throw KrakenException.badRequest("Component does not have base API docs");
    }
    return Base64.getDecoder().decode(componentAPISpecFacets.getCustomizedSpec().getContent());
  }
}
