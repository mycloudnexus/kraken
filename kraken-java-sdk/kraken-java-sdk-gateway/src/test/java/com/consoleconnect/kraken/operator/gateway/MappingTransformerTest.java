package com.consoleconnect.kraken.operator.gateway;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MappingTransformerTest extends AbstractIntegrationTest implements MappingTransformer {

  @Test
  @SneakyThrows
  void givenEmptyOrNegativeDutyFreeAmountValue_whenDeleteNode_thenReturnOK() {
    Map<String, String> checkPathMap = new HashMap<>();
    String checkPath = "$['quoteItem'][0]['quoteItemPrice'][0]['price']['dutyFreeAmount']['value']";
    String deletePath = "$.quoteItem[0].quoteItemPrice";
    checkPathMap.put(checkPath, deletePath);
    String input = readFileToString("/mockData/quoteResponseWithNegativeDutyFreeAmountValue.json");
    String result = deleteNodeByPath(checkPathMap, input);
    assertThat(result, hasNoJsonPath("$.quoteItem[0].quoteItemPrice"));
  }

  @Test
  void givenJsonInput_whenDeleteNode_thenReturnOK() {
    Map<String, String> checkPathMap = new HashMap<>();
    checkPathMap.put("$.key1", "$.key1");
    checkPathMap.put("$.key2", "$.key2");
    checkPathMap.put("$.key3", "$.key3");
    checkPathMap.put("$.key4", "$.key4");
    String input =
        "{\"key1\":\"\",\"key2\":-1,\"key3\":false,\"key4\":-2.5,\"key\":\"hello kraken\"}";
    String result = deleteNodeByPath(checkPathMap, input);
    Assertions.assertEquals("{\"key\":\"hello kraken\"}", result);
  }

  @Test
  void givenJson_whenDeleteNodeByPath_thenDeleteNodeSuccess() {
    Map<String, String> checkPathMap = new HashMap<>();
    String input =
        "{\"state\":\"completed1\",\"completionDate\":\"123\",\"productOrderItem\":[{\"state\":\"completed\",\"completionDate\":\"123\"}]}\n";
    checkPathMap.put("$[?(@.state == 'completed')]", "$.completionDate");
    checkPathMap.put(
        "$.productOrderItem[?(@.state == 'completed')]",
        "$.productOrderItem[?(@.state != 'completed')].completionDate");
    checkPathMap.put("$.notFound", "$.notFound");
    String s = deleteNodeByPath(checkPathMap, input);
    assertThat(s, hasJsonPath("$.productOrderItem[0].completionDate"), notNullValue());
  }

  @ParameterizedTest
  @MethodSource(value = "buildUnmatchedTargetMapper")
  void givenUnmatchedTargetType_whenAddTargetValueMapping_thenReturnNothing(
      ComponentAPITargetFacets.Mapper mapper) {
    StateValueMappingDto responseTargetMapperDto = new StateValueMappingDto();
    String target = "";
    addTargetValueMapping(mapper, responseTargetMapperDto, target);
    Assertions.assertTrue(MapUtils.isEmpty(responseTargetMapperDto.getTargetPathValueMapping()));
  }

  public static List<ComponentAPITargetFacets.Mapper> buildUnmatchedTargetMapper() {
    ComponentAPITargetFacets.Mapper mapper1 = new ComponentAPITargetFacets.Mapper();
    mapper1.setTargetType(MappingTypeEnum.STRING.getKind());

    ComponentAPITargetFacets.Mapper mapper2 = new ComponentAPITargetFacets.Mapper();
    mapper2.setTargetType(MappingTypeEnum.ENUM.getKind());

    ComponentAPITargetFacets.Mapper mapper3 = new ComponentAPITargetFacets.Mapper();
    return List.of(mapper1, mapper2, mapper3);
  }
}
