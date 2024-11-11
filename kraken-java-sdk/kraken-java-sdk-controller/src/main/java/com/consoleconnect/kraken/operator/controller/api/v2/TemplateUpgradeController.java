package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.controller.dto.*;
import com.consoleconnect.kraken.operator.controller.service.TemplateUpgradeService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/template-upgrade/",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Template Upgrade", description = "Template Upgrade")
@Slf4j
public class TemplateUpgradeController {
  private final TemplateUpgradeService templateUpgradeService;

  @Operation(summary = "list template upgrade deployments")
  @GetMapping("/template-deployments")
  public HttpResponse<Paging<TemplateUpgradeDeploymentVO>> listTemplateDeployment(
      @PathVariable("productId") String productId,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "templateUpgradeId", required = false) String templateUpgradeId,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {

    PageRequest pageRequest = getSearchPageRequest(page, size, direction, orderBy);
    return HttpResponse.ok(
        templateUpgradeService.listTemplateDeployment(templateUpgradeId, pageRequest));
  }

  @Operation(summary = "list of template upgrade details")
  @GetMapping("/template-deployments/{deploymentId}")
  public HttpResponse<List<MapperTagVO>> getDetail(
      @PathVariable("productId") String productId,
      @PathVariable("deploymentId") String deploymentId) {
    return HttpResponse.ok(templateUpgradeService.templateDeploymentDetails(deploymentId));
  }

  @Operation(summary = "show current upgrade version")
  @GetMapping("/current-versions")
  public HttpResponse<List<TemplateUpgradeDeploymentVO>> currentUpgradeVersion(
      @PathVariable("productId") String productId) {
    return HttpResponse.ok(templateUpgradeService.currentUpgradeVersion());
  }
}
