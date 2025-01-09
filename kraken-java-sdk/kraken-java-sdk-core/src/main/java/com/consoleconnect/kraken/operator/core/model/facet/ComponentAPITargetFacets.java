package com.consoleconnect.kraken.operator.core.model.facet;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.Data;

@Data
public class ComponentAPITargetFacets {
  private Server server;
  private List<Object> inputs;
  private Trigger trigger;
  private List<Endpoint> endpoints;

  @Data
  public static class Server {
    private String uri;
  }

  @Data
  public static class Endpoint {
    private String id;
    private String key;
    private String path;
    private String serverKey;

    private String url;
    private String pathReferId;
    private String method;
    private String requestBody;
    private String responseBody;
    private Mappers mappers;
  }

  @Data
  public static class Mappers {
    private List<Mapper> request;
    private List<Mapper> response;
  }

  @Data
  public static class Mapper {
    private String title;
    private String name;
    private String description;
    private String source;
    private String sourceType;
    private String sourceLocation;
    private List<String> sourceValues;
    private String target;
    private String targetType;
    private String targetLocation;
    private Boolean requiredMapping = Boolean.TRUE;
    private Boolean replaceStar = Boolean.TRUE;
    private Boolean allowValueLimit = Boolean.FALSE;
    private String defaultValue;
    private List<String> targetValues;
    private Map<String, String> valueMapping;
    private String function;
    private String checkPath;
    private String deletePath;
    private Boolean customizedField = false;
    private String convertValue;

    @Override
    public int hashCode() {
      return Objects.hash(
          name,
          source,
          sourceLocation,
          target,
          targetType,
          targetLocation,
          targetValues,
          valueMapping);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Mapper mapper = (Mapper) o;
      return Objects.equals(name, mapper.name)
          && Objects.equals(source, mapper.source)
          && Objects.equals(sourceLocation, mapper.sourceLocation)
          && Objects.equals(target, mapper.target)
          && Objects.equals(targetType, mapper.targetType)
          && Objects.equals(targetLocation, mapper.targetLocation)
          && Objects.equals(targetValues, mapper.targetValues)
          && Objects.equals(valueMapping, mapper.valueMapping);
    }
  }

  @Data
  public static class Trigger {
    private String productType;
    private String method;
    private String path;
    private String actionType;
    private Boolean provideAlternative;
    private String addressType;
    private String quoteLevel;
    private Boolean syncMode;
  }
}
