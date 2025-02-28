package com.consoleconnect.kraken.operator.gateway;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasNoJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import com.consoleconnect.kraken.operator.core.dto.StateValueMappingDto;
import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import com.consoleconnect.kraken.operator.core.model.KVPair;
import com.consoleconnect.kraken.operator.core.model.PathRule;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.jetbrains.annotations.NotNull;
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

    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    stateValueMappingDto.setTargetCheckPathMapper(checkPathMap);
    String result = deleteAndInsertNodeByPath(stateValueMappingDto, input);
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

    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    stateValueMappingDto.setTargetCheckPathMapper(checkPathMap);
    String result = deleteAndInsertNodeByPath(stateValueMappingDto, input);
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
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    stateValueMappingDto.setTargetCheckPathMapper(checkPathMap);
    String s = deleteAndInsertNodeByPath(stateValueMappingDto, input);
    assertThat(s, hasJsonPath("$.productOrderItem[0].completionDate"), notNullValue());
  }

  @Test
  void givenQuoteJson_whenUnableToProvide_thenDeletePathOK() {
    Map<String, String> checkPathMap = new HashMap<>();
    checkPathMap.put("$[?(@.state != 'unableToProvide')]", "$.validFor,  $.quoteLevel");
    String input =
        "{\"id\":\"id-here\",\"validFor\":{\"startDateTime\":\"123\",\"endDateTime\":\"456\"},\"quoteLevel\":\"hello\",\"state\":\"unableToProvide\"}";
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    stateValueMappingDto.setTargetCheckPathMapper(checkPathMap);
    String result = deleteAndInsertNodeByPath(stateValueMappingDto, input);
    String expected = "{\"id\":\"id-here\",\"state\":\"unableToProvide\"}";
    Assertions.assertEquals(expected, result);
  }

  @Test
  void givenPathRules_whenFilling_thenReturnOK() {
    List<PathRule> pathRules = new ArrayList<>();
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    fillPathRulesIfExist(pathRules, stateValueMappingDto);
    Assertions.assertTrue(MapUtils.isEmpty(stateValueMappingDto.getTargetCheckPathMapper()));
    PathRule pathRule = new PathRule();
    pathRule.setCheckPath("$[?(@.state != 'unableToProvide')]");
    pathRule.setDeletePath("$.validFor,  $.quoteLevel");
    pathRules.add(pathRule);
    fillPathRulesIfExist(pathRules, stateValueMappingDto);
    Assertions.assertTrue(CollectionUtils.isNotEmpty(stateValueMappingDto.getPathRules()));
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

  @Test
  void givenJson_whenAddNode_thenReturnOK() {
    String json = "{}";
    String key1 = "$.quoteItem[0].terminationError.code";
    String val1 = "otherIssue";
    String key2 = "$.quoteItem[0].terminationError.value";
    String val2 = "hello";
    String key3 = "$.state";
    String val3 = "success";
    String key4 = "$.quoteItem[0].quoteItemPrice[0].dutyFreeAmount.unit";
    String val4 = "16";
    String key5 = "$.quoteItem[0].quoteItemPrice[1].dutyFreeAmount.unit";
    String val5 = "15";

    DocumentContext doc = JsonPath.parse(json);
    ensurePathExists(doc, key1);
    ensurePathExists(doc, key2);
    ensurePathExists(doc, key3);
    ensurePathExists(doc, key4);
    ensurePathExists(doc, key5);
    doc.set(key1, val1);
    doc.set(key2, val2);
    doc.set(key3, val3);
    doc.set(key4, val4);
    doc.set(key5, val5);
    String result = doc.jsonString();
    Assertions.assertNotNull(result);
    assertThat(result, hasJsonPath("$.quoteItem", hasSize(1)));
    assertThat(result, hasJsonPath(key1, notNullValue()));
    assertThat(result, hasJsonPath(key2, notNullValue()));
    assertThat(result, hasJsonPath(key3, notNullValue()));
    assertThat(result, hasJsonPath(key4, notNullValue()));
    assertThat(result, hasJsonPath(key5, notNullValue()));
  }

  @Test
  @SneakyThrows
  void givenJson_whenDeleteAndInsertNodeByPath_thenReturnOK() {
    String json = readFileToString("mockData/quote.eline.response.json");
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    PathRule pathRule = getPathRule();
    pathRule.setCheckPath("$[?(@.state != 'unableToProvide')]");
    String d1 = "$.validFor";
    String d2 = "$.quoteLevel";
    pathRule.setDeletePath(d1 + ",    " + d2);

    List<KVPair> insertPath = new ArrayList<>();
    KVPair p1 = new KVPair();
    String key1 = "$.quoteItem[0].terminationError.code";
    p1.setKey(key1);
    p1.setVal("otherIssue");
    KVPair p2 = new KVPair();
    String key2 = "$.quoteItem[0].terminationError.value";
    p2.setKey(key2);
    p2.setVal("the quoted item is not available");

    insertPath.add(p1);
    insertPath.add(p2);

    pathRule.setInsertPath(insertPath);
    List<PathRule> pathRules = new ArrayList<>();
    pathRules.add(pathRule);
    fillPathRulesIfExist(pathRules, stateValueMappingDto);

    String key3 = "$['quoteItem'][0]['quoteItemPrice'][0]['price']['dutyFreeAmount']['value']";
    String val3 = "$.quoteItem[0].quoteItemPrice";
    stateValueMappingDto.getTargetCheckPathMapper().put(key3, val3);
    String result = deleteAndInsertNodeByPath(stateValueMappingDto, json);
    Assertions.assertNotNull(result);

    assertThat(result, hasJsonPath(key1, notNullValue()));
    assertThat(result, hasJsonPath(key2, notNullValue()));
    assertThat(result, hasJsonPath(val3, notNullValue()));
    assertThat(result, hasNoJsonPath(d1));
    assertThat(result, hasNoJsonPath(d2));
  }

  @Test
  @SneakyThrows
  void givenCanProvideQuote_whenInsertNodes_thenReturnOK() {
    String json = readFileToString("mockData/quote.eline.can.provide.response.json");
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    PathRule pathRule = getPathRule();
    List<PathRule> pathRules = new ArrayList<>();
    pathRules.add(pathRule);
    fillPathRulesIfExist(pathRules, stateValueMappingDto);

    String result = deleteAndInsertNodeByPath(stateValueMappingDto, json);
    Assertions.assertNotNull(result);
    assertThat(result, hasNoJsonPath("$.quoteItem[0].terminationError"));
  }

  private static @NotNull PathRule getPathRule() {
    PathRule pathRule = new PathRule();
    pathRule.setCheckPath("$[?(@.state != 'unableToProvide')]");
    String d1 = "$.validFor";
    String d2 = "$.quoteLevel";
    pathRule.setDeletePath(d1 + ",    " + d2);

    List<KVPair> insertPath = new ArrayList<>();
    KVPair p1 = new KVPair();
    String key1 = "$.quoteItem[0].terminationError.code";
    p1.setKey(key1);
    p1.setVal("otherIssue");
    KVPair p2 = new KVPair();
    String key2 = "$.quoteItem[0].terminationError.value";
    p2.setKey(key2);
    p2.setVal("the quoted item is not available");

    insertPath.add(p1);
    insertPath.add(p2);

    pathRule.setInsertPath(insertPath);
    return pathRule;
  }

  @Test
  void givenEmptyCheckRules_whenDeleteNodes_thenReturnOK() {
    String json = "Are you ok?";
    StateValueMappingDto stateValueMappingDto = new StateValueMappingDto();
    String result = deleteAndInsertNodeByPath(stateValueMappingDto, json);
    Assertions.assertEquals(json, result);
  }
}
