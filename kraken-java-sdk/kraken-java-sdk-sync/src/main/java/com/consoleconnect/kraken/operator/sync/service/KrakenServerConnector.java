package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.IpUtils;
import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import java.net.URI;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

@Slf4j
@Service
public class KrakenServerConnector {

  @Getter private final SyncProperty appProperty;
  private final WebClient webClient;

  private volatile ExternalSystemTokenProvider agentTokenProvider = null;

  public static final String CLIENT_ID = IpUtils.getHostAddress();

  public KrakenServerConnector(SyncProperty appProperty, WebClient webClient) {
    this.appProperty = appProperty;
    this.webClient = webClient;
  }

  public HttpResponse<Void> curl(HttpMethod method, String path, Object body) {
    return curl(method, path, body, res -> {});
  }

  public HttpResponse<Void> curl(
      HttpMethod method, String path, Object body, Consumer<HttpResponse<Void>> onSuccess) {
    HttpResponse<Void> res = curl(method, path, body, new ParameterizedTypeReference<>() {});
    if (onSuccess != null && res.getCode() == 200) {
      onSuccess.accept(res);
    }
    return res;
  }

  public <T> HttpResponse<T> curl(
      HttpMethod method,
      String path,
      Object body,
      ParameterizedTypeReference<HttpResponse<T>> responseBodyType) {
    log.info("[start]curl: method={}, path={}", method, path);
    HttpResponse<T> res =
        blockCurl(
            method,
            uriBuilder -> uriBuilder.path(path).build(),
            this.getToken(),
            body,
            responseBodyType);
    if (res.getCode() == 200) {
      log.info("[{}]curl: method={}, path={}", res.getCode(), method, path);
    } else {
      log.error("[{}]curl: method={}, path={}", res.getCode(), method, path);
    }
    return res;
  }

  public <T> HttpResponse<T> blockCurl(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      Object body,
      ParameterizedTypeReference<HttpResponse<T>> responseBodyType) {
    return blockCurl(method, uriFunction, this.getToken(), body, responseBodyType);
  }

  public <T> HttpResponse<T> blockCurl(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      String accessToken,
      Object body,
      ParameterizedTypeReference<HttpResponse<T>> responseBodyType) {
    try {
      return reactiveCurl(method, uriFunction, accessToken, body, responseBodyType).block();
    } catch (WebClientResponseException ex) {
      return HttpResponse.of(ex.getStatusCode().value(), ex.getMessage(), null);
    } catch (Exception ex) {
      return HttpResponse.of(500, ex.getMessage(), null);
    }
  }

  public <T> Mono<HttpResponse<T>> reactiveCurl(
      HttpMethod method,
      Function<UriBuilder, URI> uriFunction,
      String accessToken,
      Object body,
      ParameterizedTypeReference<HttpResponse<T>> responseBodyType) {
    WebClient.RequestBodySpec requestBodySpec =
        webClient
            .method(method)
            .uri(uriFunction)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON);
    if (body != null) {
      requestBodySpec.body(BodyInserters.fromValue(body));
    }
    if (accessToken != null) {
      requestBodySpec.header(appProperty.getControlPlane().getTokenHeader(), accessToken);
    }

    return requestBodySpec.retrieve().bodyToMono(responseBodyType);
  }

  public HttpResponse<Void> pushEvent(ClientEvent clientEvent) {
    return curl(HttpMethod.POST, appProperty.getControlPlane().getPushEventEndpoint(), clientEvent);
  }

  private String getToken() {
    return getAgentTokenProvider().getToken();
  }

  private ExternalSystemTokenProvider getAgentTokenProvider() {
    if (this.agentTokenProvider != null) {
      return this.agentTokenProvider;
    }
    synchronized (this) {
      if (this.agentTokenProvider == null) {
        this.agentTokenProvider =
            ApplicationContextProvider.getApplicationContext()
                .getBean(ExternalSystemTokenProvider.class);
      }
      return agentTokenProvider;
    }
  }
}
