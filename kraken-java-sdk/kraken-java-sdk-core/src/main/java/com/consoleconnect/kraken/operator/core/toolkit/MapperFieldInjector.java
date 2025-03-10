package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import java.util.Map;
import java.util.function.BiConsumer;

public class MapperFieldInjector {
  private MapperFieldInjector() {}

  private static final Map<String, BiConsumer<String, ComponentAPITargetFacets.Mapper>> injectMap =
      Map.of();
}
