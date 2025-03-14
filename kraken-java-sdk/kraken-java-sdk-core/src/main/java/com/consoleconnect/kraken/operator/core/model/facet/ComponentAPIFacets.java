package com.consoleconnect.kraken.operator.core.model.facet;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ComponentAPIFacets {
  private APISpec apiSpec;
  private List<RouteMapping> mappings;
  private List<SupportedProductAndAction> supportedProductTypesAndActions;
  private ApiTargetMapping apiTargetMapping;

  @Data
  public static class APISpec {
    private String key;
  }

  @Data
  public static class RouteMapping {
    private String uri;
    private Trigger trigger;
    private List<Action> actions;
    private Metadata metadata;
  }

  @Data
  public static class Metadata {
    private boolean cacheRequestBody;

    private Headers requestHeaders;
    private Headers responseHeaders;

    private Long responseTimeout;
    private Long connectTimeout;
  }

  @Data
  public static class Headers {
    private Map<String, String> add;
    private List<String> delete;
  }

  @Data
  public static class Trigger {
    private String path;
    private String method;
    private List<String> queryParams;
  }

  @Data
  public static class Action {
    private String id;
    private String actionType;

    @JsonAlias({"if", "condition", "when"})
    @JsonProperty("if")
    private String condition;

    private Map<String, String> env;
    private Map<String, Object> with;
    private String outputKey;

    private boolean preRequest = true;
    private boolean postRequest;
    private int order = 0;

    private String name;
    private String pattern;
    private String path;
    private String method;

    private Body requestBody;
    private HttpResponse response;

    private List<Filter> filters;
    private boolean postResultRender;
  }

  @Data
  public static class Filter {

    private String id;
    private String name;

    @JsonAlias({"if", "condition", "when"})
    @JsonProperty("if")
    private String condition;

    private String path;
    private String method;

    private Body requestBody;
    private HttpResponse response;
  }

  @Data
  public static class HttpResponse {
    private String code;
    private Body body;
  }

  @Data
  public static class Body {
    private String transformerKey;
    private String message;
    private String reason;
  }

  @Data
  public static class ApiTargetMapping {
    List<String> keys;
  }

  @Data
  public static class SupportedProductAndAction {
    private List<String> actionTypes;
    private String path;
    private String method;
    private List<String> productTypes;
  }
}
