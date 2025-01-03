package com.consoleconnect.kraken.operator.core.model.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.conductor.common.metadata.tasks.TaskType;
import com.netflix.conductor.common.metadata.workflow.WorkflowTask;
import com.netflix.conductor.sdk.workflow.def.tasks.Task;
import com.netflix.conductor.sdk.workflow.utils.ObjectMapperProvider;
import java.util.Arrays;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
@Setter
public class HttpExtend extends Task<HttpExtend> {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpExtend.class);

  private static final String INPUT_PARAM = "http_request";

  private ObjectMapper objectMapper = new ObjectMapperProvider().getObjectMapper();

  private Input httpRequest;

  public HttpExtend(String taskReferenceName) {
    super(taskReferenceName, TaskType.HTTP);
    this.httpRequest = new Input();
    this.httpRequest.method = Input.HttpMethod.GET;
    super.input(INPUT_PARAM, httpRequest);
  }

  public HttpExtend input(HttpExtend.Input httpRequest) {
    this.httpRequest = httpRequest;
    return this;
  }

  public HttpExtend url(String url) {
    this.httpRequest.setUri(url);
    return this;
  }

  public HttpExtend method(HttpExtend.Input.HttpMethod method) {
    this.httpRequest.setMethod(method);
    return this;
  }

  public HttpExtend headers(Object headers) {
    this.httpRequest.setHeaders(headers);
    return this;
  }

  public HttpExtend body(Object body) {
    this.httpRequest.setBody(body);
    return this;
  }

  public HttpExtend readTimeout(int readTimeout) {
    this.httpRequest.setReadTimeOut(readTimeout);
    return this;
  }

  @Override
  protected void updateWorkflowTask(WorkflowTask workflowTask) {
    workflowTask.getInputParameters().put(INPUT_PARAM, httpRequest);
  }

  @Getter
  @Setter
  public static class Input {
    public enum HttpMethod {
      PUT,
      POST,
      GET,
      DELETE,
      OPTIONS,
      HEAD;

      public static HttpMethod getIgnoreCase(String name) {
        return Arrays.stream(HttpMethod.values())
            .filter(e -> e.name().equalsIgnoreCase(name))
            .findAny()
            .orElse(null);
      }
    }

    private HttpMethod method; // PUT, POST, GET, DELETE, OPTIONS, HEAD
    private String vipAddress;
    private String appName;
    private Object headers;
    private String uri;
    private Object body;
    private String accept = "application/json";
    private String contentType = "application/json";
    private Integer connectionTimeOut;
    private Integer readTimeOut;
  }
}
