package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.core.enums.ParamLocationEnum.*;
import static com.consoleconnect.kraken.operator.core.toolkit.ConstructExpressionUtil.*;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentWorkflowFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RenderRequestService implements MappingTransformer {

  private final UnifiedAssetService unifiedAssetService;

  public RenderRequestService(UnifiedAssetService unifiedAssetService) {
    this.unifiedAssetService = unifiedAssetService;
  }

  public void handlePath(List<ComponentAPITargetFacets.Endpoint> endpoints) {
    ComponentAPITargetFacets.Mappers mappers = endpoints.get(0).getMappers();
    String path = endpoints.get(0).getPath();
    if (mappers != null && mappers.getRequest() != null) {
      List<ComponentAPITargetFacets.Mapper> request = mappers.getRequest();

      for (ComponentAPITargetFacets.Mapper mapper : request) {
        log.info("parse mapper name: {}", mapper.getName());
        String sourceLocation = mapper.getSourceLocation();
        String source = mapper.getSource();
        String targetLocation = mapper.getTargetLocation();
        String target = mapper.getTarget();
        boolean needConvertFromDB = StringUtils.isNotBlank(mapper.getConvertValue());
        if (needConvertFromDB) {
          log.info("require to convert value from db");
          handlePathRefer(mapper);
          source = mapper.getSource();
        }
        List<String> pathParams = extractMapperParam(target);
        if (!pathParams.isEmpty()) {
          List<String> params = extractMapperParam(source);
          if (CollectionUtils.isEmpty(params)) {
            continue;
          }
          path =
              convertPath(
                  params,
                  targetLocation,
                  path,
                  sourceLocation,
                  pathParams,
                  needConvertFromDB,
                  target);
        }
      }
    }
    endpoints.get(0).setPath(path);
  }

  private String convertPath(
      List<String> params,
      String targetLocation,
      String path,
      String sourceLocation,
      List<String> pathParams,
      boolean needConvertFromDB,
      String target) {
    String s = params.get(0);
    if (Objects.equals(PATH.name(), targetLocation)) {
      path = whenTargetPath(sourceLocation, path, pathParams, s, needConvertFromDB);
    } else if (Objects.equals(QUERY.name(), targetLocation)) {
      path = whenTargetQuery(path, sourceLocation, pathParams, s, needConvertFromDB);
    } else if (Objects.equals(HYBRID.name(), targetLocation)) {
      path = whenTargetHYBRID(path, target);
    }
    return path;
  }

  public void parseRequest(
      List<ComponentAPITargetFacets.Endpoint> endpoints,
      StateValueMappingDto stateValueMappingDto) {
    handlePath(endpoints);
    handleBody(endpoints, stateValueMappingDto);
  }

  private void handleBody(
      List<ComponentAPITargetFacets.Endpoint> endpoints,
      StateValueMappingDto stateValueMappingDto) {
    String requestBody = endpoints.get(0).getRequestBody();
    fillPathRulesIfExist(endpoints.get(0).getMappers().getPathRules(), stateValueMappingDto);
    List<ComponentAPITargetFacets.Mapper> request = endpoints.get(0).getMappers().getRequest();
    if (CollectionUtils.isEmpty(request)) {
      return;
    }
    for (ComponentAPITargetFacets.Mapper mapper : request) {
      if (Objects.equals(BODY.name(), mapper.getTargetLocation())) {
        // Skipping constant target
        if (StringUtils.isBlank(mapper.getTarget())
            || !mapper.getTarget().startsWith(REPLACEMENT_KEY_PREFIX)) {
          log.info("handleBody skip source:{}, target:{}", mapper.getSource(), mapper.getTarget());
          continue;
        }
        String source = constructBody(mapper.getSource());
        requestBody =
            JsonToolkit.generateJson(
                convertToJsonPointer(mapper.getTarget().replace(REQUEST_BODY, StringUtils.EMPTY)),
                source,
                requestBody);
        log.info(
            "handleBody inserted source:{}, target:{}, requestBody:{}",
            source,
            mapper.getTarget(),
            requestBody);
        if (MappingTypeEnum.ENUM.getKind().equalsIgnoreCase(mapper.getSourceType())
            && StringUtils.isNotBlank(mapper.getTarget())
            && MapUtils.isNotEmpty(mapper.getValueMapping())) {
          stateValueMappingDto
              .getTargetPathValueMapping()
              .put(convertTarget(mapper.getTarget()), mapper.getValueMapping());
        }
      } else {
        log.info(
            "handleBody skip source:{}, target:{} with no body",
            mapper.getSource(),
            mapper.getTarget());
      }
    }
    endpoints.get(0).setRequestBody(requestBody);
    log.info("handleBody rendered request body:{}", requestBody);
  }

  private void handlePathRefer(ComponentAPITargetFacets.Mapper mapper) {
    String convertValue = mapper.getConvertValue();
    String[] pathReferIds = convertValue.split("#");
    UnifiedAsset asset = unifiedAssetService.findOne(pathReferIds[0]);
    ComponentAPITargetFacets createFacets =
        UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
    Optional<ComponentAPITargetFacets.Mapper> uniqueMapper;
    if (createFacets.getWorkflow() != null && createFacets.getWorkflow().isEnabled()) {
      UnifiedAssetDto workflowAsset =
          unifiedAssetService.findOne(createFacets.getWorkflow().getKey());
      ComponentWorkflowFacets workflowFacets =
          UnifiedAsset.getFacets(workflowAsset, ComponentWorkflowFacets.class);
      ComponentAPITargetFacets.Mappers mappers =
          workflowFacets.getExecutionStage().get(0).getEndpoint().getMappers();
      Optional<ComponentAPITargetFacets.Mapper> uniqueOpt =
          mappers.getResponse().stream()
              .filter(respMapper -> Objects.equals(respMapper.getName(), pathReferIds[1]))
              .findFirst();
      uniqueMapper = uniqueOpt;
    } else {
      List<ComponentAPITargetFacets.Mapper> response =
          createFacets.getEndpoints().get(0).getMappers().getResponse();
      Optional<ComponentAPITargetFacets.Mapper> instanceOpt =
          response.stream().filter(v -> Objects.equals(pathReferIds[1], v.getName())).findFirst();
      uniqueMapper = instanceOpt;
    }

    if (uniqueMapper.isPresent()) {
      ComponentAPITargetFacets.Mapper referMapper = uniqueMapper.get();
      String source = referMapper.getSource();
      List<String> paramLocations = extractMapperParam(source);
      mapper.setSource(constructOriginalDBParam(paramLocations.get(0)));
      log.info("converted value: {}", mapper.getSource());
    }
  }

  private String whenTargetPath(
      String sourceLocation,
      String path,
      List<String> pathParams,
      String s,
      boolean needConvertFromDB) {
    if (needConvertFromDB) {
      return path.replace("{" + pathParams.get(0) + "}", constructParam(s));
    }
    if (Objects.equals(QUERY.name(), sourceLocation)) {
      path = path.replace("{" + pathParams.get(0) + "}", constructMefQuery(s));
    } else if (Objects.equals(BODY.name(), sourceLocation)) {
      path = path.replace("{" + pathParams.get(0) + "}", constructMeRequestBody(s));
    } else if (Objects.equals(PATH.name(), sourceLocation)) {
      path = path.replace("{" + pathParams.get(0) + "}", constructParam(s));
    }
    return path;
  }

  private String whenTargetQuery(
      String path,
      String sourceLocation,
      List<String> pathParams,
      String s,
      boolean needConvertFromDB) {
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder
        .append(path)
        .append((path.contains("?") ? "&" : "?"))
        .append(pathParams.get(0))
        .append("=");
    if (Objects.equals(QUERY.name(), sourceLocation)) {
      path =
          pathBuilder
              .append(needConvertFromDB ? constructParam(s) : constructMefQuery(s))
              .toString();
    } else if (Objects.equals(BODY.name(), sourceLocation)) {
      path = pathBuilder.append(constructMeRequestBody(s)).toString();
    }
    return path;
  }

  private String whenTargetHYBRID(String path, String target) {
    StringBuilder pathBuilder = new StringBuilder();
    return pathBuilder
        .append(path)
        .append((path.contains("?") ? "&" : "?"))
        .append(constructBody(target))
        .toString();
  }
}
