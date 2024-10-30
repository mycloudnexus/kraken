package com.consoleconnect.kraken.operator.auth.controller;

import com.consoleconnect.kraken.operator.auth.dto.CreateUserRequest;
import com.consoleconnect.kraken.operator.auth.dto.ResetPasswordRequest;
import com.consoleconnect.kraken.operator.auth.dto.UpdateUserRequest;
import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import com.consoleconnect.kraken.operator.auth.model.User;
import com.consoleconnect.kraken.operator.auth.model.UserLoginEnabled;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.auth.service.UserService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@ConditionalOnBean(UserLoginEnabled.class)
@AllArgsConstructor
@RestController()
@RequestMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "User Mgmt", description = "User Mgmt")
public class UserMgmtController {

  private final UserService userService;

  @Operation(summary = "List users")
  @GetMapping
  public HttpResponse<Paging<User>> search(
      @RequestParam(value = "q", required = false) String q,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size,
      @RequestParam(value = "filterInternalUser", required = false, defaultValue = "false")
          Boolean filterInternalUser) {
    return HttpResponse.ok(
        userService.search(
            q, UnifiedAssetService.getSearchPageRequest(page, size), filterInternalUser));
  }

  @Operation(summary = "add a new user")
  @PostMapping()
  public Mono<HttpResponse<User>> create(@RequestBody CreateUserRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> userService.create(request, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "get user by id")
  @GetMapping("/{id}")
  public HttpResponse<User> findOne(@PathVariable String id) {
    return HttpResponse.ok(userService.findOne(id));
  }

  @Operation(summary = "update user by id")
  @PatchMapping("/{id}")
  public Mono<HttpResponse<User>> update(
      @PathVariable String id, @RequestBody UpdateUserRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> userService.update(id, request, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "reset password for a user")
  @PostMapping("/{id}/resetPassword")
  public Mono<HttpResponse<User>> resetPassword(
      @PathVariable String id, @RequestBody ResetPasswordRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> userService.resetPassword(id, request.getPassword(), userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Disable a user")
  @PatchMapping("/{id}/disable")
  public Mono<HttpResponse<User>> disable(@PathVariable String id) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> userService.updateState(id, UserStateEnum.DISABLED, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Enable a user")
  @PatchMapping("/{id}/enable")
  public Mono<HttpResponse<User>> enable(@PathVariable String id) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> userService.updateState(id, UserStateEnum.ENABLED, userId))
        .map(HttpResponse::ok);
  }

  /**
   * Only refresh tokens can be revoked, access tokens are stateless and cannot be revoked, it still
   * can work util expired
   */
  @Operation(summary = "Revoke a user's tokens")
  @PostMapping("/{userId}/revokeTokens")
  public Mono<HttpResponse<Void>> revokeTokens(@PathVariable String userId) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .doOnNext(loginUserId -> userService.revokeUserTokens(userId, loginUserId))
        .then(Mono.just(HttpResponse.ok(null)));
  }
}
