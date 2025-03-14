package com.consoleconnect.kraken.operator.core.model.facet;

import com.consoleconnect.kraken.operator.core.enums.SupportedCaseEnum;
import com.consoleconnect.kraken.operator.core.model.CommonMapperRef;
import com.consoleconnect.kraken.operator.core.model.PathRule;
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
  private Workflow workflow;
  private SupportedCase supportedCase = new SupportedCase();

  @Data
  public static class Server {
    private String uri;
  }

  @Data
  public static class SupportedCase {
    private String type = SupportedCaseEnum.ONE_TO_ONE.name();
  }

  @Data
  public static class Workflow {
    private boolean enabled;
    private boolean synchronous;
    private String id;
    private String key;
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
    private List<PathRule> pathRules;
    private CommonMapperRef schemaRef;
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
    private String sourceConditionExpression;
    private List<SourceCondition> sourceConditions;
    private Boolean allowValueLimit = Boolean.FALSE;
    private Boolean discrete;
    private List<String> sourceValues;
    private String target;
    private String targetType;
    private String targetLocation;
    private Boolean requiredMapping = Boolean.TRUE;
    private Boolean replaceStar = Boolean.TRUE;
    private String defaultValue;
    private List<String> targetValues;
    private Map<String, String> valueMapping;
    private String function;
    private String checkPath;
    private String deletePath;
    private Boolean customizedField = false;
    private String convertValue;

    private static final String MAPPER_REQUEST = "request";
    private static final String MAPPER_RESPONSE = "response";

    public String getKey(String mapperSection) {
      final int hashcode;
      if (Objects.equals(MAPPER_REQUEST, mapperSection)) {
        hashcode = Objects.hash(mapperSection, source, sourceLocation);
      } else {
        hashcode = Objects.hash(mapperSection, target, targetLocation);
      }
      return String.valueOf(hashcode);
    }

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

  @Data
  public static class SourceCondition {
    private String name;
    private String key;
    private String val;
    private String operator;
  }
}
