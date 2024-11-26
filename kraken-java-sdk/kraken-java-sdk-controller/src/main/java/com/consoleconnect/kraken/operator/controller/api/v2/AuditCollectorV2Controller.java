package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.toolkit.AssetsConstants.FIELD_UPDATE_AT_ORIGINAL;

import com.consoleconnect.kraken.operator.controller.dto.APIServerEnvDTO;
import com.consoleconnect.kraken.operator.controller.enums.SystemStateEnum;
import com.consoleconnect.kraken.operator.controller.model.Environment;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.service.*;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.AssetKindEnum;
import com.consoleconnect.kraken.operator.core.enums.EnvNameEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.SystemInfoFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.fasterxml.jackson.core.type.TypeReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
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

  private final APITokenService apiTokenService;
  private final BuyerService buyerService;
  private final ProductDeploymentService productDeploymentService;
  private final Environment2APIServerCache environment2APIServerCache;
  private final UnifiedAssetService unifiedAssetService;
  private final SystemInfoService systemInfoService;
  private final EnvironmentService environmentService;

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
    } else if (AssetKindEnum.PRODUCT_COMPATIBILITY.getKind().equals(kind)) {
      List<UnifiedAssetDto> assetDtos =
          unifiedAssetService.findByKind(AssetKindEnum.PRODUCT_COMPATIBILITY.getKind()).stream()
              .findFirst()
              .map(List::of)
              .orElse(List.of());
      return HttpResponse.ok(assetDtos);
    } else if (AssetKindEnum.SYSTEM_INFO.getKind().equals(kind)) {

      UnifiedAssetDto unifiedAssetDto = buildSystemInfo(envId);
      return HttpResponse.ok(List.of(unifiedAssetDto));
    } else {
      return HttpResponse.of(
          HttpStatus.NOT_IMPLEMENTED.value(),
          HttpStatus.NOT_IMPLEMENTED.getReasonPhrase(),
          "not implemented");
    }
  }

  private UnifiedAssetDto buildSystemInfo(String envId) {
    SystemInfo systemInfo = systemInfoService.find();
    Environment environment = environmentService.findOne(envId);
    UnifiedAsset unifiedAsset =
        UnifiedAsset.of(
            AssetKindEnum.SYSTEM_INFO.getKind(), AssetKindEnum.SYSTEM_INFO.getKind(), "");
    UnifiedAssetDto unifiedAssetDto = new UnifiedAssetDto();
    BeanUtils.copyProperties(unifiedAsset, unifiedAssetDto);
    unifiedAssetDto.setKind(AssetKindEnum.SYSTEM_INFO.getKind());
    unifiedAssetDto.getMetadata().setKey(AssetKindEnum.SYSTEM_INFO.getKind());
    SystemInfoFacets systemInfoFacets = new SystemInfoFacets();
    if (EnvNameEnum.STAGE.name().equalsIgnoreCase(environment.getName())) {
      if (SystemStateEnum.STAGE_UPGRADING.name().equalsIgnoreCase(systemInfo.getStatus())) {
        systemInfoFacets.setStatus(SystemInfoFacets.SystemStatus.UPGRADING);
      } else {
        systemInfoFacets.setStatus(SystemInfoFacets.SystemStatus.RUNNING);
      }
      systemInfoFacets.setAppVersion(systemInfo.getStageAppVersion());
      systemInfoFacets.setProductVersion(systemInfo.getStageProductVersion());
    } else {
      if (SystemStateEnum.PRODUCTION_UPGRADING.name().equalsIgnoreCase(systemInfo.getStatus())) {
        systemInfoFacets.setStatus(SystemInfoFacets.SystemStatus.UPGRADING);
      } else {
        systemInfoFacets.setStatus(SystemInfoFacets.SystemStatus.RUNNING);
      }
      systemInfoFacets.setAppVersion(systemInfo.getProductionAppVersion());
      systemInfoFacets.setProductVersion(systemInfo.getProductionProductVersion());
    }
    unifiedAssetDto.setFacets(
        JsonToolkit.fromJson(systemInfoFacets, new TypeReference<Map<String, Object>>() {}));
    return unifiedAssetDto;
  }
}
