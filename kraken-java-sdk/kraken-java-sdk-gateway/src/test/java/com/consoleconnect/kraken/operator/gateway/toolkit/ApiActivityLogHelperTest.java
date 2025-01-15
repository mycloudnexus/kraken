package com.consoleconnect.kraken.operator.gateway.toolkit;

import com.consoleconnect.kraken.operator.core.model.ApiActivityRequestLog;
import com.consoleconnect.kraken.operator.core.model.ApiActivityResponseLog;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.workflow.model.LogTaskRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

public class ApiActivityLogHelperTest {

  @Test
  void testExtractRequestPayload() {
    LogTaskRequest logTaskRequest = new LogTaskRequest();
    logTaskRequest.setRequestPayload(readFileToString("mockData/workflowRequest.json"));
    ApiActivityRequestLog requestLog = ApiActivityLogHelper.extractRequestLog(logTaskRequest);
    Assertions.assertEquals("https://localhost/qe1company/ports/order?q=v", requestLog.getUri());
    Assertions.assertEquals("/qe1company/ports/order", requestLog.getPath());
    Assertions.assertEquals("PUT", requestLog.getMethod());
    Assertions.assertEquals("v", requestLog.getQueryParameters().get("q"));
    Assertions.assertEquals("HEADER_VAL1", requestLog.getHeaders().get("TEST_HEADER"));

    Map<String, Object> body =
        (Map<String, Object>) JsonToolkit.fromJson((String) requestLog.getRequest(), Map.class);
    Assertions.assertEquals("test-qc01", body.get("portName"));
  }

  @Test
  void testExtractResponsePayload() {
    LogTaskRequest logTaskRequest = new LogTaskRequest();
    logTaskRequest.setResponsePayload(readFileToString("mockData/workflowResponse.json"));
    ApiActivityResponseLog responseLog = ApiActivityLogHelper.extractResponseLog(logTaskRequest);
    Assertions.assertNull(responseLog.getResponseIp());
    Assertions.assertEquals(200, responseLog.getHttpStatusCode());

    Map<String, Object> body =
        (Map<String, Object>) JsonToolkit.fromJson((String) responseLog.getResponse(), Map.class);
    Assertions.assertEquals("Console Connect - Hermes House", body.get("name"));
  }

  @SneakyThrows
  private String readFileToString(String path) {
    ResourceLoader resourceLoader = new DefaultResourceLoader();
    Resource resource = resourceLoader.getResource(path);
    return IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
  }
}
