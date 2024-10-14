package com.consoleconnect.kraken.operator.core.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class Metadata {
  private String id;
  private String name;
  private int version;

  private String key;
  private String description;

  private String productKey;
  private String mapperKey;

  @JsonAlias({"icon", "logo"})
  @JsonProperty("logo")
  private String logo;

  private String status;

  private Set<String> tags;
  private Map<String, String> labels = new HashMap<>();
  private String referApiSpec;
  private String referWorkflow;
}
