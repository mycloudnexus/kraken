package com.consoleconnect.kraken.operator.auth.controller;

import com.consoleconnect.kraken.operator.auth.dto.*;
import com.consoleconnect.kraken.operator.auth.model.BasicUserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.model.User;
import com.consoleconnect.kraken.operator.auth.model.UserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@ConditionalOnBean({UserLoginEnabled.class, BasicUserLoginEnabled.class})
@AllArgsConstructor
@RestController()
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Auth", description = "User Auth")
public class UserAuthController {

  private final UserService userService;

  @Operation(summary = "login with user name")
  @PostMapping(value = "/login")
  public HttpResponse<AuthResponse> login(@RequestBody AuthRequest authenticationRequest) {
    return HttpResponse.ok(userService.login(authenticationRequest));
  }

  @Operation(summary = "generate access token")
  @PostMapping(value = "/auth/token")
  public HttpResponse<AuthResponse> onAuth(@RequestBody AuthRequest request) {
    return HttpResponse.ok(userService.onAuth(request));
  }

  @Operation(summary = "reset password")
  @PostMapping("/auth/resetPassword")
  public Mono<HttpResponse<User>> resetPassword(@RequestBody ResetPasswordRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> userService.resetPassword(userId, request.getPassword(), userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Current access token details")
  @GetMapping("/userinfo")
  public Mono<HttpResponse<User>> getUserInfo() {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userService::findOne)
        .map(HttpResponse::ok);
  }

  /**
   * Only refresh tokens can be revoked, access tokens are stateless and cannot be revoked, it still
   * can work util expired
   */
  @Operation(summary = "Revoke all refresh tokens")
  @PostMapping("/auth/revokeTokens")
  public Mono<HttpResponse<Void>> revokeAll() {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(userId -> userService.revokeUserTokens(userId, userId))
        .then(Mono.just(HttpResponse.ok(null)));
  }
}
