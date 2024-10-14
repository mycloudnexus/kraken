package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.core.enums.ParamLocationEnum.*;

import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RenderRequestService {

  private final UnifiedAssetService unifiedAssetService;

  public RenderRequestService(UnifiedAssetService unifiedAssetService) {
    this.unifiedAssetService = unifiedAssetService;
  }

  public void parseRequest(ComponentAPITargetFacets facets) {
    handlePath(facets);
    handleBody(facets);
  }

  private void handlePath(ComponentAPITargetFacets facets) {
    List<ComponentAPITargetFacets.Endpoint> endpoints = facets.getEndpoints();
    String path = endpoints.get(0).getPath();
    ComponentAPITargetFacets.Mappers mappers = endpoints.get(0).getMappers();
    if (mappers != null && mappers.getRequest() != null) {
      List<ComponentAPITargetFacets.Mapper> request = mappers.getRequest();
      // replace last position of path param when pathReferId is not null
      path = handlePathRefer(endpoints.get(0));

      for (ComponentAPITargetFacets.Mapper mapper : request) {
        log.info("parse mapper name: {}", mapper.getName());
        String target = mapper.getTarget();
        String targetLocation = mapper.getTargetLocation();
        String source = mapper.getSource();
        String sourceLocation = mapper.getSourceLocation();
        List<String> pathParams = extractMapperParam(target);
        if (!pathParams.isEmpty()) {
          List<String> params = extractMapperParam(source);
          String s = params.get(0);
          if (Objects.equals(PATH.name(), targetLocation)) {
            path = whenTargetPath(sourceLocation, path, pathParams, s);
          } else if (Objects.equals(QUERY.name(), targetLocation)) {
            path = whenTargetQuery(path, sourceLocation, pathParams, s);
          } else if (Objects.equals(HYBRID.name(), targetLocation)) {
            path = whenTargetHYBRID(path, target);
            break;
          }
        }
      }
    }
    endpoints.get(0).setPath(path);
  }

  private void handleBody(ComponentAPITargetFacets facets) {
    String requestBody = facets.getEndpoints().get(0).getRequestBody();
    List<ComponentAPITargetFacets.Mapper> request =
        facets.getEndpoints().get(0).getMappers().getRequest();
    if (StringUtils.isBlank(requestBody) || CollectionUtils.isEmpty(request)) {
      return;
    }
    Map<String, Object> map = null;
    try {
      map = JsonToolkit.fromJson(requestBody, Map.class);
    } catch (Exception e) {
      return;
    }
    for (ComponentAPITargetFacets.Mapper mapper : request) {
      if (Objects.equals(BODY.name(), mapper.getTargetLocation())) {
        String target = extractMapperParam(mapper.getTarget()).get(0);
        String[] keys = target.split("\\.");
        String source = constructBodyWithoutBrace(mapper.getSource());
        if (!StringUtils.isBlank(mapper.getFunction())) {
          source = replaceFunction(mapper.getFunction(), source);
        }
        setValueViaPath(new LinkedList<>(Arrays.asList(keys)), map, source);
      }
    }
    facets.getEndpoints().get(0).setRequestBody(JsonToolkit.toJson(map));
  }

  public static void setValueViaPath(List<String> keys, Map<String, Object> map, String value) {
    if (keys.size() == 1) {
      map.put(keys.get(0), value);
      return;
    }
    String s = keys.get(0);
    keys.remove(s);
    setValueViaPath(keys, (Map) map.get(s), value);
  }

  public static String replaceFunction(String function, String value) {
    return constructParam(function.replace("source", value));
  }

  private String handlePathRefer(ComponentAPITargetFacets.Endpoint endpoint) {
    if ((endpoint.getPathReferId() != null)) {
      String pathReferId = endpoint.getPathReferId();
      String path = endpoint.getPath();
      String[] pathReferIds = pathReferId.split("#");
      UnifiedAsset asset = unifiedAssetService.findOne(pathReferIds[0]);
      ComponentAPITargetFacets createFacets =
          UnifiedAsset.getFacets(asset, ComponentAPITargetFacets.class);
      List<ComponentAPITargetFacets.Mapper> response =
          createFacets.getEndpoints().get(0).getMappers().getResponse();
      Optional<ComponentAPITargetFacets.Mapper> instanceOpt =
          response.stream().filter(v -> Objects.equals(pathReferIds[1], v.getName())).findFirst();
      List<String> pathParams = extractOriginalPathParam(path);
      if (instanceOpt.isPresent()) {
        ComponentAPITargetFacets.Mapper mapper = instanceOpt.get();
        String source = mapper.getSource();
        List<String> paramLocations = extractMapperParam(source);
        return path.replace(
            "{" + pathParams.get(pathParams.size() - 1) + "}",
            constructDBParam(paramLocations.get(0)));
      }
    }
    return endpoint.getPath();
  }

  private String whenTargetPath(
      String sourceLocation, String path, List<String> pathParams, String s) {
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
      String path, String sourceLocation, List<String> pathParams, String s) {
    StringBuilder pathBuilder = new StringBuilder();
    pathBuilder
        .append(path)
        .append((path.contains("?") ? "&" : "?"))
        .append(pathParams.get(0))
        .append("=");
    if (Objects.equals(QUERY.name(), sourceLocation)) {
      path = pathBuilder.append(constructMefQuery(s)).toString();
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

  private static String constructMefQuery(String s) {
    return String.format("${mefQuery.%s}", s);
  }

  private static String constructMeRequestBody(String s) {
    return String.format("${mefRequestBody.%s}", s);
  }

  private static String constructParam(String s) {
    return String.format("${%s}", s);
  }

  private static String constructBody(String source) {
    return source.replace("@{{", "${mefRequestBody.").replace("}}", "}");
  }

  private static String constructBodyWithoutBrace(String source) {
    return source.replace("@{{", "mefRequestBody.").replace("}}", "");
  }

  private static String constructDBParam(String s) {
    return String.format("${entity.response.%s}", s.replace("responseBody.", ""));
  }

  private static List<String> extractParam(String param, String patternStr) {
    List<String> contents = new ArrayList<>();
    Pattern pattern = Pattern.compile(patternStr);
    Matcher matcher = pattern.matcher(param);
    while (matcher.find()) {
      contents.add(matcher.group(1));
    }
    return contents;
  }

  private static List<String> extractOriginalPathParam(String path) {
    String patternStr = "\\{(.*?)\\}";
    return extractParam(path, patternStr);
  }

  private static List<String> extractMapperParam(String param) {
    String patternStr = "\\@\\{\\{(.*?)\\}\\}";
    return extractParam(param, patternStr);
  }
}
