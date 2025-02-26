package com.consoleconnect.kraken.operator.core.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class StateValueMappingDto {

  /** <target-path, <mapping-key, mapping-value>> */
  private Map<String, Map<String, String>> targetPathValueMapping = new HashMap<>();

  /** delete specified field when key filed is empty */
  private Map<String, String> targetCheckPathMapper = new HashMap<>();

  private List<PathRuleDto> pathRules = new ArrayList<>();
}
