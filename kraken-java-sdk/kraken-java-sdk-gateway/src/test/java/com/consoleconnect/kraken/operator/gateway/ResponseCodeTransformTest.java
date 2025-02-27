package com.consoleconnect.kraken.operator.gateway;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.model.HttpResponseContext;
import com.consoleconnect.kraken.operator.gateway.runner.ResponseCodeTransform;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import java.util.List;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.context.ContextConfiguration;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ResponseCodeTransformTest extends AbstractIntegrationTest implements ResponseCodeTransform {
  @Test
  void testResponseCodeTransform() {
    String res =
        """
                {
                "result":"targetKey:notFound"
                }
                """;
    assertThrows(KrakenException.class, () -> checkOutputKey(res));
  }

  @Test
  void given400_whenRewriteStatus_thenReturnOK() {
    ResponseCodeTransform responseCodeTransform = new ResponseCodeTransform() {};
    HttpResponseContext httpResponseContext = new HttpResponseContext();
    httpResponseContext.setStatus(400);
    responseCodeTransform.rewriteStatus(httpResponseContext);
    Assertions.assertEquals(422, httpResponseContext.getStatus());
  }

  @SneakyThrows
  @Test
  void givenErrorJson_whenDeleteStatus_thenReturnOK() {
    String json = readFileToString("mockData/downstream.error.json");
    HttpResponseContext httpResponseContext = new HttpResponseContext();
    httpResponseContext.setStatus(422);
    httpResponseContext.setBody(JsonToolkit.fromJson(json, new TypeReference<>() {}));
    List<String> list = List.of("$.error.status", "$.error.statusCode");
    httpResponseContext.setDeletePaths(list);
    Assertions.assertThrowsExactly(
        KrakenException.class, () -> checkResponseCode(httpResponseContext));
  }

  @SneakyThrows
  @Test
  void givenErrorJson_whenDeletes_thenReturnOK() {
    String json = readFileToString("mockData/downstream.error.json");
    DocumentContext doc = JsonPath.parse(json);
    List<String> list = List.of("$.error.status", "$.error.statusCode");
    list.forEach(
        item -> {
          deleteByPath(item, doc);
        });
    String result = doc.jsonString();
    System.out.println(result);
    Assertions.assertNotNull(result);
  }
}
