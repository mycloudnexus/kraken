package com.consoleconnect.kraken.operator.gateway.toolkit;

import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.GATEWAY_SERVICE;

import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import io.jsonwebtoken.lang.Strings;
import io.netty.util.internal.StringUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApiActivityLogHelper {

  private ApiActivityLogHelper() {}

  private static final String INPUT_PARAM_REQUEST = "http_request";
  private static final String INPUT_PARAM_RESPONSE = "response";
  private static final String INPUT_PARAM_URI = "uri";
  private static final String INPUT_PARAM_METHOD = "method";
  private static final String INPUT_PARAM_HEADERS = "headers";
  private static final String INPUT_PARAM_BODY = "body";
  private static final String INPUT_PARAM_STATUS_CODE = "statusCode";

  public static ApiActivityRequestLog extractRequestLog(LogTaskRequest payload) {
    if (payload.getRequestPayload() == null) {
      return null;
    }
    Map<String, Object> map = convertToMap(payload.getRequestPayload());
    Map<String, Object> request = convertToMap(map.get(INPUT_PARAM_REQUEST));
    String url = getAsString(request, INPUT_PARAM_URI);
    return ApiActivityRequestLog.builder()
        .requestId(payload.getRequestId())
        .uri(url)
        .path(parsePath(url))
        .method(getAsString(request, INPUT_PARAM_METHOD))
        .queryParameters(parseQueryParams(url))
        .headers(getMultiValMap(request, INPUT_PARAM_HEADERS))
        .request(JsonToolkit.toJson(getAsMap(request, INPUT_PARAM_BODY)))
        .requestIp(GATEWAY_SERVICE)
        .build();
  }

  public static ApiActivityResponseLog extractResponseLog(LogTaskRequest payload) {
    if (payload.getResponsePayload() == null) {
      log.warn("No response payload");
      return null;
    }
    Map<String, Object> map = convertToMap(payload.getResponsePayload());
    if (!map.containsKey(INPUT_PARAM_RESPONSE)) {
      return ApiActivityResponseLog.builder().build();
    }
    Object orgResp = map.get(INPUT_PARAM_RESPONSE);
    if (orgResp instanceof String) {
      return ApiActivityResponseLog.builder().build();
    }
    Map<String, Object> response = convertToMap(orgResp);
    return ApiActivityResponseLog.builder()
        .response(JsonToolkit.toJson(getAsMap(response, INPUT_PARAM_BODY)))
        .httpStatusCode(getAsInt(response, INPUT_PARAM_STATUS_CODE))
        .build();
  }

  private static Map<String, Object> convertToMap(Object payload) {
    if (payload instanceof String strPayload) {
      return JsonToolkit.fromJson(strPayload, new TypeReference<Map<String, Object>>() {});
    } else if (payload instanceof Map<?, ?> mapPayload) {
      return (Map<String, Object>) mapPayload;
    } else {
      return JsonToolkit.fromJson(
          JsonToolkit.toJson(payload), new TypeReference<Map<String, Object>>() {});
    }
  }

  private static String parsePath(String query) {
    try {
      URL url = new URL(query);
      return url.getPath();
    } catch (MalformedURLException e) {
      return StringUtil.EMPTY_STRING;
    }
  }

  private static Map<String, String> parseQueryParams(String query) {
    try {
      URL url = new URL(query);
      String queryString = url.getQuery();

      if (queryString == null) {
        return Map.of();
      }
      Map<String, String> params = new HashMap<>();
      String[] parameters = queryString.split("&");
      for (String parameter : parameters) {
        String[] keyValue = parameter.split("=");
        String key = keyValue[0];
        String value = keyValue.length > 1 ? keyValue[1] : "";
        params.put(key, value);
      }
      return params;
    } catch (MalformedURLException e) {
      return Map.of();
    }
  }

  private static String getAsString(Map<String, Object> map, String key) {
    return map.containsKey(key) ? (String) map.get(key) : null;
  }

  private static Integer getAsInt(Map<String, Object> map, String key) {
    return map.containsKey(key) ? (Integer) map.get(key) : null;
  }

  private static Map<String, String> getAsMap(Map<String, Object> map, String key) {
    if (!map.containsKey(key)) {
      return Map.of();
    }
    return (Map<String, String>) map.get(key);
  }

  private static Map<String, String> getMultiValMap(Map<String, Object> map, String key) {
    if (!map.containsKey(key)) {
      return Map.of();
    }
    Map<String, Object> convertedMap = (Map<String, Object>) map.get(key);
    Map<String, String> result = new HashMap<>(convertedMap.size());
    convertedMap.forEach(
        (k, v) -> {
          if (v instanceof String strVal) {
            result.put(k, strVal);
          } else if (v instanceof List list) {
            result.put(k, String.join(",", list));
          } else {
            result.put(k, Strings.EMPTY + v);
          }
        });
    return result;
  }
}
