package com.consoleconnect.kraken.operator.controller.api.v2;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.handler.ClientAPIAuditLogEventHandler;
import com.consoleconnect.kraken.operator.controller.service.*;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/client", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Client PubSub", description = "Client PubSub")
@Slf4j
public class ClientPubSubController {

  private final APITokenService apiTokenService;

  private final ClientEventService clientEventService;

  private final ClientAPIAuditLogEventHandler clientAPIAuditLogEventHandler;

  @Operation(summary = "receive a event from client")
  @PostMapping("/events")
  public Mono<HttpResponse<Void>> onEvent(
      @RequestParam(value = "env", required = false) String env,
      @Autowired(required = false) JwtAuthenticationToken authenticationToken,
      @RequestBody ClientEvent event) {
    String envId = apiTokenService.findEnvId(authenticationToken, env);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> clientEventService.onEvent(envId, userId, event));
  }

  @Operation(summary = "receive a event from client")
  @PostMapping("/test")
  public Mono<HttpResponse<Void>> test(@RequestParam(value = "count", required = false) int count) {
    this.clientAPIAuditLogEventHandler.onEvent(count);
    return null;
  }
}
