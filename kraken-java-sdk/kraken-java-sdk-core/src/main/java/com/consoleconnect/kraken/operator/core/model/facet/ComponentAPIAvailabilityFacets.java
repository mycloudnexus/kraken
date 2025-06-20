package com.consoleconnect.kraken.operator.core.model.facet;

import java.util.*;
import lombok.Data;

@Data
public class ComponentAPIAvailabilityFacets {
  private Set<String> stageDisableApiList = new HashSet<>();
  private Set<String> prodDisableApiList = new HashSet<>();
}
