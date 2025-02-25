package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.mapper.FacetsMapper;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapperCopyTest {

  @Test
  void givenPathRules_whenCopyEndpoints_thenReturnOK() {
    ComponentAPITargetFacets.Endpoint source = getSourceEndpoint();

    ComponentAPITargetFacets.Endpoint target = new ComponentAPITargetFacets.Endpoint();
    FacetsMapper.INSTANCE.toEndpoint(source, target);
    String result = JsonToolkit.toJson(target);
    String expected =
        "{\"mappers\":{\"pathRules\":[{\"name\":\"test\",\"checkPath\":\"test\",\"deletePath\":\"test\"}]}}";
    Assertions.assertEquals(expected, result);
  }

  private static ComponentAPITargetFacets.@NotNull Endpoint getSourceEndpoint() {
    ComponentAPITargetFacets.Endpoint source = new ComponentAPITargetFacets.Endpoint();
    List<ComponentAPITargetFacets.PathRule> pathRules = new ArrayList<>();
    ComponentAPITargetFacets.PathRule pathRule = new ComponentAPITargetFacets.PathRule();
    pathRule.setName("test");
    pathRule.setCheckPath("test");
    pathRule.setDeletePath("test");
    pathRules.add(pathRule);
    ComponentAPITargetFacets.Mappers mappers = new ComponentAPITargetFacets.Mappers();
    mappers.setPathRules(pathRules);
    source.setMappers(mappers);
    return source;
  }
}
