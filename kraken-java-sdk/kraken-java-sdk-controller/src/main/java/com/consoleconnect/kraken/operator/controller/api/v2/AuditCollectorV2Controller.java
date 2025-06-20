package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_UPDATE_AT_ORIGINAL;

import com.consoleconnect.kraken.operator.controller.dto.APIServerEnvDTO;
import com.consoleconnect.kraken.operator.controller.service.*;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/v2/callback/audits", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Audits V2", description = "Audit Mgmt V2")
@Slf4j
public class AuditCollectorV2Controller {

  private final UnifiedAssetService unifiedAssetService;
  private final APITokenService apiTokenService;
  private final BuyerService buyerService;
  private final ProductDeploymentService productDeploymentService;
  private final Environment2APIServerCache environment2APIServerCache;

  @Operation(summary = "Retrieve a release with api component detail")
  @GetMapping("/releases/{releaseId}/components")
  public HttpResponse<List<UnifiedAssetDto>> queryContentByToken(
      @PathVariable("releaseId") String productReleaseId,
      @RequestParam(value = "env", required = false) String env,
      @Autowired(required = false) JwtAuthenticationToken authenticationToken) {
    String envId = apiTokenService.findEnvId(authenticationToken, env);
    log.info("queryContentByToken, productReleaseId:{}, envId:{}", productReleaseId, envId);
    return HttpResponse.ok(productDeploymentService.queryDeployedAssets(productReleaseId));
  }

  @Operation(summary = "Retrieve latest product deployment released id")
  @GetMapping("/deployments/latest")
  public HttpResponse<String> queryLatestDeployment(
      @RequestParam(value = "env", required = false) String env,
      @Autowired(required = false) JwtAuthenticationToken authenticationToken) {
    String envId = apiTokenService.findEnvId(authenticationToken, env);
    return HttpResponse.ok(productDeploymentService.findLatestInProcessDeployment(envId));
  }

  @Operation(summary = "Retrieve api servers in the specific env")
  @GetMapping("/api-servers")
  public HttpResponse<List<APIServerEnvDTO>> listApiServers(
      @RequestParam(value = "env", required = false) String env,
      @Autowired(required = false) JwtAuthenticationToken authenticationToken) {
    String envId = apiTokenService.findEnvId(authenticationToken, env);
    return HttpResponse.ok(environment2APIServerCache.getAPIServerEnvs(envId));
  }

  @Operation(summary = "Retrieve asset from control plane in the specific env")
  @GetMapping("/sync-server-asset")
  public HttpResponse<Object> syncServerAsset(
      @RequestParam(value = "kind") String kind,
      @RequestParam(value = "updatedAt", required = false) ZonedDateTime lastUpdateTime,
      @RequestParam(value = "env", required = false) String env,
      @Autowired(required = false) JwtAuthenticationToken authenticationToken) {
    String envId = apiTokenService.findEnvId(authenticationToken, env);
    if (AssetKindEnum.COMPONENT_API_SERVER.getKind().equals(kind)) {
      List<APIServerEnvDTO> apiServerEnvs = environment2APIServerCache.getAPIServerEnvs(envId);
      return HttpResponse.ok(apiServerEnvs);
    } else if (AssetKindEnum.PRODUCT_BUYER.getKind().equals(kind)) {
      log.info(
          "syncServerAsset start to query buyers, envId:{}, lastUpdateTime:{}",
          envId,
          lastUpdateTime);
      Paging<UnifiedAssetDto> pages =
          buyerService.search(
              null,
              envId,
              null,
              null,
              lastUpdateTime,
              PageRequest.of(0, 100, Sort.Direction.ASC, FIELD_UPDATE_AT_ORIGINAL));
      log.info(
          "syncServerAsset end to query buyers, result:{}",
          Objects.nonNull(pages) ? JsonToolkit.toJson(pages.getData()) : "");
      return HttpResponse.ok(Objects.nonNull(pages) ? pages.getData() : List.of());
    } else if (AssetKindEnum.COMPONENT_SELLER_CONTACT.getKind().equals(kind)) {
      List<UnifiedAssetDto> assetDtoList =
          unifiedAssetService.findByKind(AssetKindEnum.COMPONENT_SELLER_CONTACT.getKind());
      return HttpResponse.ok(assetDtoList);
    } else if (AssetKindEnum.COMPONENT_API_AVAILABILITY.getKind().equals(kind)) {
      List<UnifiedAssetDto> assetDtoList =
          unifiedAssetService.findByKind(AssetKindEnum.COMPONENT_API_AVAILABILITY.getKind());
      return HttpResponse.ok(assetDtoList);
    } else {
      return HttpResponse.of(
          HttpStatus.NOT_IMPLEMENTED.value(),
          HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(),
          "not implemented");
    }
  }
}
