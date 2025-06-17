package com.consoleconnect.kraken.operator.gateway;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.hasJsonPath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.UnifiedAsset;
import com.consoleconnect.kraken.operator.core.model.facet.ComponentAPITargetFacets;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.YamlToolkit;
import com.consoleconnect.kraken.operator.gateway.func.SpelFunc;
import com.consoleconnect.kraken.operator.gateway.runner.MappingTransformer;
import com.consoleconnect.kraken.operator.gateway.template.SpELEngine;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class SpELEngineTest extends AbstractIntegrationTest implements MappingTransformer {
  @SpyBean private UnifiedAssetService unifiedAssetService;
  @Autowired SpelFunc spelFunc;

  @Test
  void givenDateTimePlus_whenEvaluate_thenReturnOK() {
    String s1 =
        "${T(com.consoleconnect.kraken.operator.core.toolkit.DateTime).nowInUTCFormatted()}";
    Object obj1 = SpELEngine.evaluate(s1, new HashMap<>());
    System.out.println(obj1);
    Assertions.assertNotNull(obj1);

    String s2 =
        "${T(com.consoleconnect.kraken.operator.core.toolkit.DateTime).nowInUTCFormatted('5', T(java.time.temporal.ChronoUnit).DAYS)}";
    Object obj2 = SpELEngine.evaluate(s2, new HashMap<>());
    System.out.println(obj2);
    Assertions.assertNotNull(obj2);
  }

  @Test
  void testRenderArray() {
    String s = "[{\"id\":\"67ad55a4d3044d46e53dc5ab\"}]";
    String expression = "${renderedResponseBody[0].id?:''}";
    Map<String, Object> map =
        Map.of("renderedResponseBody", JsonToolkit.fromJson(s, new TypeReference<List>() {}));
    Object obj = SpELEngine.evaluateWithoutSuppressException(expression, map, Object.class);
    Assertions.assertEquals("67ad55a4d3044d46e53dc5ab", obj);
  }

  @Test
  void testIt() {
    Map<String, Object> data = new HashMap<>();

    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("buyerId", "100");
    data.put("query", queryParams);

    Map<String, Object> body = new HashMap<>();
    body.put("action", "create");
    data.put("body", body);

    SpELEngine engine = new SpELEngine(data);
    Assertions.assertTrue(engine.isTrue("${body.action == 'create' && query.buyerId == '100'}"));
    Assertions.assertTrue(engine.isTrue("${body.action == 'create' || query.buyerId == '200'}"));
    Assertions.assertFalse(engine.isTrue("${body.action == 'create' && query.buyerId == '200'}"));
  }

  @Test
  void testRenderJsonString() throws IOException {

    String apiConfigYamlString = readFileToString("mef/order/api.target.order.yaml");

    UnifiedAsset asset = YamlToolkit.parseYaml(apiConfigYamlString, UnifiedAsset.class).get();
    ComponentAPITargetFacets facets =
        UnifiedAsset.getFacets(asset, new TypeReference<ComponentAPITargetFacets>() {});

    Map<String, Object> variables = new HashMap<>();

    // env
    Map<String, Object> env = new HashMap<>();
    env.put("targetAPIServer", "http://localhost:8080");
    variables.put("env", env);

    // mefQuery
    Map<String, Object> mefQuery = new HashMap<>();
    mefQuery.put("buyerId", "100");
    variables.put("mefQuery", mefQuery);

    assertEquals(
        "http://localhost:8080",
        SpELEngine.evaluate(facets.getServer().getUri(), variables, String.class));

    for (ComponentAPITargetFacets.Endpoint endpoint : facets.getEndpoints()) {
      String mefRequestBody;
      if (endpoint.getPath().contains("/ports/")) {
        // mefRequestBody
        mefRequestBody = readFileToString("mef/order/orderPortBody.json");
      } else {
        // mefRequestBody
        mefRequestBody = readFileToString("mef/order/orderConnectionBody.json");
      }
      variables.put(
          "mefRequestBody",
          YamlToolkit.parseYaml(mefRequestBody, Object.class)
              .orElseThrow(() -> KrakenException.notFound("mefRequestBody")));

      String errorResp = readFileToString("mef/order/not_login_401.json");
      variables.put(
          "response",
          YamlToolkit.parseYaml(errorResp, Object.class)
              .orElseThrow(() -> KrakenException.notFound("errorResp")));

      String expression =
          YamlToolkit.toYaml(endpoint).orElseThrow(() -> KrakenException.internalError(""));
      System.out.println(expression);
      String renderedEndpoint = SpELEngine.evaluate(expression, variables, String.class);
      ComponentAPITargetFacets.Endpoint renderedEndpointObj =
          YamlToolkit.parseYaml(renderedEndpoint, ComponentAPITargetFacets.Endpoint.class)
              .orElseThrow(() -> KrakenException.notFound("renderedEndpoint"));
      Assertions.assertNotNull(renderedEndpointObj.getKey());
      Assertions.assertNotNull(renderedEndpointObj.getMethod());
      Assertions.assertNotNull(renderedEndpointObj.getPath());
      Assertions.assertNotNull(renderedEndpointObj.getRequestBody());
    }
  }

  public static String readFileToString(String path) throws IOException {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(path);
    return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
  }

  @Test
  void testIfExpression() throws IOException {
    Map<String, Object> data = new HashMap<>();

    String bodyJsonString = readFileToString("mef/order/orderPortBody.json");
    data.put("body", JsonToolkit.fromJson(bodyJsonString, Object.class));

    SpELEngine engine = new SpELEngine(data);
    Assertions.assertTrue(
        engine.isTrue(
            "${body['productOrderItem'][0]['product']['productConfiguration']['@type'] == 'UNI' && body['productOrderItem'][0]['action'] == 'add'}"));
  }

  @Test
  void testExtractValues() {
    String pattern = "/mefApi/sonata/geographicSiteManagement/v7/geographicSite/(?<segment>.*)";
    String input = "/mefApi/sonata/geographicSiteManagement/v7/geographicSite/123";

    // Convert the pattern into a regular expression

    Pattern r = Pattern.compile(pattern);
    Matcher m = r.matcher(input);

    if (m.find()) {
      // Get the names of the capture groups from the pattern
      Pattern groupNamesPattern = Pattern.compile("\\(\\?<(.+?)>");
      Matcher groupNamesMatcher = groupNamesPattern.matcher(pattern);

      // Print all the variables that were found
      while (groupNamesMatcher.find()) {
        String variableName = groupNamesMatcher.group(1);
        String variableValue = m.group(variableName);
        System.out.println(variableName + ": " + variableValue);
        assertEquals("segment", variableName);
        assertEquals("123", variableValue);
      }
    } else {
      Assertions.fail("No match found");
    }
  }

  @Test
  void testConstructObject() throws IOException {
    String expression = readFileToString("mef/order/quoteExpression");
    String context = readFileToString("mef/order/quoteResponseBodyEline.json");
    Map map = JsonToolkit.fromJson(context, Map.class);
    Object evaluate = SpELEngine.evaluate(expression, map);
    Assertions.assertNotNull(evaluate);
  }

  @Test
  void test_nested_map_spel() throws IOException {
    String expression = readFileToString("mef/order/nestedExpression");
    String context = readFileToString("mef/order/quoteRequestBody.json");
    Map map = JsonToolkit.fromJson(context, Map.class);
    String evaluate = SpELEngine.evaluate(expression, map);
    System.out.println(JsonToolkit.toJson(evaluate));
    Assertions.assertNotNull(evaluate);
  }

  @Test
  void test_lists() throws IOException {
    String s = readFileToString("/mef/order/test_list.json");
    String context = readFileToString("mef/order/orderPortBody.json");
    Map map = JsonToolkit.fromJson(context, Map.class);
    String evaluate = SpELEngine.evaluate(s, map);
    System.out.println(evaluate);
    SpelFunc.appendSellerInformation("role", "name", "address", "1234", new ArrayList<>());
    Assertions.assertNotNull(evaluate);
  }

  @Test
  void test_dataType() throws IOException {
    String expression = readFileToString("mef/order/quoteDataTypeTest.json");
    System.out.println(expression);
    String s = readFileToString("mef/order/quoteRequestBody.json");
    String evaluate = SpELEngine.evaluate(expression, JsonToolkit.fromJson(s, Map.class));
    String evaluate1 = SpELEngine.evaluate(evaluate, JsonToolkit.fromJson(s, Map.class));
    Map map = JsonToolkit.fromJson(evaluate1, Map.class);
    Assertions.assertNotNull(map);
  }

  @Test
  void test_parse_to_list() throws IOException {
    String s = readFileToString("mockData/addressResult.json");
    String expression = readFileToString("mockData/address.list.template.json");
    Map<String, Object> context = Map.of("responseBody", JsonToolkit.fromJson(s, Object.class));
    Object o = JsonToolkit.fromJson(expression, Object.class);
    SpELEngine.parseToList(o, context, new ArrayList<>());
    String s1 = calculateBasedOnResponseBody(JsonToolkit.toJson(o), context);
    Assertions.assertNotNull(s1);
  }

  @Test
  @SneakyThrows
  void givenMapExpression_whenEvaluate_thenReturnSuccess() {
    String s = readFileToString("mockData/address.list.mock.json");
    Object evaluate =
        SpELEngine.evaluate(JsonToolkit.fromJson(s, Object.class), new HashMap<>(), true);
    assertThat(
        String.valueOf(evaluate), hasJsonPath("$.alternateGeographicAddress[0].tags", hasSize(1)));
  }

  @Test
  void givenProductConfiguration_whenAppendFromResponseMapping_thenSuccess() throws IOException {
    UnifiedAssetDto dto = new UnifiedAssetDto();
    dto.setFacets(JsonToolkit.fromJson(readFileToString("/mockData/facetData.json"), Map.class));
    Mockito.doReturn(dto).when(unifiedAssetService).findOne(anyString());
    Map<String, Object> map = new HashMap<>();
    map.put("type", "UNI");
    Map<String, Object> resultMap =
        spelFunc.appendFromResponseMapping(
            map, "productConfiguration", "mef.sonata.api-target-mapper.inventory.uni.read");
    Assertions.assertTrue(resultMap.containsKey("ccProductOrderId"));
  }
}
