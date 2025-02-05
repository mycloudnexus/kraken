package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.enums.AssetKindEnum.COMPONENT_API_SERVER;

import com.consoleconnect.kraken.operator.core.dto.SimpleApiServerDto;
import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.ActionTypeEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.AppProperty;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPIFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.gateway.service.RenderRequestService;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.fasterxml.jackson.core.type.TypeReference;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ServerWebExchange;

@Service
@Slf4j
public class LoadTargetAPIConfigActionRunner extends AbstractActionRunner
    implements SellerContactInjector {

  public static final String INPUT_CONFIG_KEY = "configKey";
  public static final String INPUT_RENDER = "render";
  public static final String URLS = "urls";
  @Getter private final UnifiedAssetService unifiedAssetService;
  private final RenderRequestService renderRequestService;

  public LoadTargetAPIConfigActionRunner(
      AppProperty appProperty,
      UnifiedAssetService unifiedAssetService,
      RenderRequestService renderRequestService) {
    super(appProperty);
    this.unifiedAssetService = unifiedAssetService;
    this.renderRequestService = renderRequestService;
  }

  @Override
  public boolean canHandle(ComponentAPIFacets.Action action) {
    return ActionTypeEnum.LOAD_TARGET_API_CONFIG
        == ActionTypeEnum.fromString(action.getActionType());
  }

  @Override
  public Optional<ServerWebExchange> runIt(
      ServerWebExchange exchange,
      ComponentAPIFacets.Action action,
      Map<String, Object> inputs,
      Map<String, Object> outputs) {

    String configKey = (String) inputs.get(INPUT_CONFIG_KEY);
    Boolean render = (Boolean) inputs.get(INPUT_RENDER);

    UnifiedAsset asset = unifiedAssetService.findOne(configKey);
    ComponentAPITargetFacets facets = UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    // merge mapper and base template file
    mergeMappers(asset, facets);

    /*String serverKey = facets.getEndpoints().get(0).getServerKey();
    if (StringUtils.isNotBlank(facets.getEndpoints().get(0).getUrl())) {
      outputs.put(
          "url", SpELEngine.evaluate(facets.getEndpoints().get(0).getUrl(), inputs, String.class));
    } else {
      // serverKey
      String serverUrl = getServerUrl(serverKey);
      outputs.put("url", serverUrl);
    }*/

    // Mock codes
    String serverUrl = "https://api.stage.consoleconnect.com";
    outputs.put("url", serverUrl);

    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    renderRequestService.parseRequest(facets, stateValueMappingDto);
    // replace env.seller if the seller contact key exists
    inject(inputs, asset.getMetadata().getKey());
    if (render != null && render) {
      facets
          .getEndpoints()
          .forEach(
              endpoint -> {
                if (Objects.nonNull(endpoint.getRequestBody())) {
                  String replaced = replaceStar(endpoint.getRequestBody());
                  String renderedRequest =
                      renderStatus(stateValueMappingDto, SpELEngine.evaluate(replaced, inputs));
                  endpoint.setRequestBody(renderedRequest);
                }
                if (Objects.nonNull(endpoint.getResponseBody())) {
                  String transformedResp = transform(endpoint, stateValueMappingDto);
                  endpoint.setResponseBody(SpELEngine.evaluate(transformedResp, inputs));
                }
                if (Objects.nonNull(endpoint.getPath())) {
                  String evaluate =
                      SpELEngine.evaluate(replaceStar(endpoint.getPath()), inputs, String.class);
                  endpoint.setPath(encodeUrlParam(evaluate));
                }
              });
    }
    outputs.put(
        KrakenFilterConstants.X_KRAKEN_TARGET_VALUE_MAPPER,
        JsonToolkit.toJson(stateValueMappingDto));

    outputs.put(action.getOutputKey(), JsonToolkit.toJson(facets));
    return Optional.empty();
  }

  public static String encodeUrlParam(String path) {
    String[] split = path.split("\\?");
    StringBuilder pathBuilder = new StringBuilder().append(split[0]);
    if (split.length > 1) {
      pathBuilder.append("?");
      String params = split[1];
      List<String> list = Arrays.stream(params.split("&")).distinct().toList();
      for (String param : list) {
        String[] splitArr = param.split("=");
        pathBuilder.append(splitArr[0]).append("=");
        if (splitArr.length > 1) {
          pathBuilder.append(URLEncoder.encode(splitArr[1], StandardCharsets.UTF_8));
        }
        pathBuilder.append("&");
      }
    }
    return pathBuilder.toString();
  }

  public void mergeMappers(UnifiedAsset asset, ComponentAPITargetFacets facets) {
    if (asset.getMetadata() == null || asset.getMetadata().getMapperKey() == null) {
      log.warn("not found mapperKey");
      return;
    }
    String mapperKey = asset.getMetadata().getMapperKey();
    UnifiedAsset mapper = unifiedAssetService.findOne(mapperKey);
    if (mapper == null) {
      log.warn("mapper not found {}", mapperKey);
    }
    ComponentAPITargetFacets mapperFacets =
        UnifiedAsset.getFacets(mapper, ComponentAPITargetFacets.class);
    mergeMapper(facets, mapperFacets.getEndpoints().get(0));
  }

  public String getServerUrl(String serverKey) {
    UnifiedAssetDto assetDto = unifiedAssetService.findOne(COMPONENT_API_SERVER.getKind());
    List<SimpleApiServerDto> serverDtoList =
        JsonToolkit.fromJson(
            JsonToolkit.toJson(assetDto.getFacets().get(URLS)),
            new TypeReference<List<SimpleApiServerDto>>() {});
    if (CollectionUtils.isEmpty(serverDtoList)) {
      throw KrakenException.internalError("serverKey not found");
    }
    return serverDtoList.stream()
        .filter(serverDto -> Objects.equals(serverDto.getApiServerKey(), serverKey))
        .map(SimpleApiServerDto::getUrl)
        .findFirst()
        .orElseThrow(() -> KrakenException.notFound("the target url is not found"));
  }
}
