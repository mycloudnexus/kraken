package com.consoleconnect.kraken.operator.gateway.service;

import static com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants.X_KRAKEN_URL;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogBodyRepository;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.net.URI;
import lombok.SneakyThrows;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.http.server.reactive.MockServerHttpResponse;
import org.springframework.mock.web.server.MockServerWebExchange;

@MockIntegrationTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BackendServerLogServiceTest extends AbstractIntegrationTest {

  @Autowired private BackendServerLogService backendServerLogService;

  @SpyBean private ApiActivityLogBodyRepository apiActivityLogBodyRepository;

  @Test
  @Order(1)
  @SneakyThrows
  void givenApiCalled_whenGetApiActivityLogs_thenReturnOK() {
    String url = "https://test.com";
    URI uri = URI.create(url);
    MockServerWebExchange exchange =
        MockServerWebExchange.from(MockServerHttpRequest.method(HttpMethod.GET, uri));
    exchange.getAttributes().put(X_KRAKEN_URL, url);
    exchange.getAttributes().put(KrakenFilterConstants.X_LOG_REQUEST_ID, "test-id");

    backendServerLogService.recordRequestParameter(exchange);

    Route route = Mockito.mock(Route.class);
    Mockito.doReturn(uri).when(route).getUri();
    exchange.getAttributes().put(GATEWAY_ROUTE_ATTR, route);
    backendServerLogService.recordRequestBody(exchange, () -> "{}");

    MockServerHttpResponse mockResponse = exchange.getResponse();
    mockResponse.setStatusCode(HttpStatus.OK);
    mockResponse.setComplete();
    backendServerLogService.recordResponse(exchange, () -> "{}");
    Mockito.verify(apiActivityLogBodyRepository, Mockito.times(3)).save(Mockito.any());
  }
}
