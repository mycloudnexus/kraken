package com.consoleconnect.kraken.operator.gateway;

import com.consoleconnect.kraken.operator.gateway.model.HttpContext;
import com.consoleconnect.kraken.operator.gateway.model.HttpRequestContext;
import com.consoleconnect.kraken.operator.gateway.template.JavaScriptEngine;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class JavaScriptEngineTest {
  @Test
  void testJavaScriptFunction() throws IOException {
    JavaScriptEngine engine = new JavaScriptEngine();
    engine.addSource(
        "function.js", IOUtils.resourceToString("/javascript/function.js", StandardCharsets.UTF_8));
    HttpContext context = new HttpContext();
    HttpRequestContext request = new HttpRequestContext();
    request.setMethod("GET");
    request.setPath("/orders");
    request.setUri("http://localhost:8080");
    context.setRequest(request);
    String result = engine.execute("function.js", context);

    Assertions.assertNotNull(result);
  }

  @Test
  void testJavaScriptArrowFunction() throws IOException {
    JavaScriptEngine engine = new JavaScriptEngine();
    engine.addSource(
        "arrowFunction.js",
        IOUtils.resourceToString("/javascript/arrowFunction.js", StandardCharsets.UTF_8));
    HttpContext context = new HttpContext();
    HttpRequestContext request = new HttpRequestContext();
    request.setMethod("GET");
    request.setPath("/orders");
    request.setUri("http://localhost:8080");
    context.setRequest(request);
    String result = engine.execute("arrowFunction.js", context);
    Assertions.assertNotNull(result);
  }
}
