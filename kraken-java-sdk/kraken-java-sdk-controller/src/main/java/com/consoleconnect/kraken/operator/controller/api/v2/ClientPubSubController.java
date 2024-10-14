package com.consoleconnect.kraken.operator.controller.api.v2;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.service.*;
import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
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

  @Operation(summary = "receive a event from client")
  @PostMapping("/events")
  public Mono<HttpResponse<Void>> onEvent(
      @RequestParam(value = "env", required = false) String env,
      @RequestHeader(value = "Authorization", required = false) String authorization,
      @RequestBody ClientEvent event) {
    String envId = apiTokenService.findEnvId(authorization, env);
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> clientEventService.onEvent(envId, userId, event));
  }
}
