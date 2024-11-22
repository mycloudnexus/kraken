package com.consoleconnect.kraken.operator.controller.api;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.push.ApiRequestActivityPushResult;
import com.consoleconnect.kraken.operator.controller.dto.push.CreatePushApiActivityRequest;
import com.consoleconnect.kraken.operator.controller.dto.push.PushApiActivityLogEnabled;
import com.consoleconnect.kraken.operator.controller.dto.push.PushApiActivityLogHistory;
import com.consoleconnect.kraken.operator.controller.service.push.ApiActivityPushService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.request.PushLogSearchRequest;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/push-api-activity-log", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "API Activities Push Logs", description = "API Activities Push Logs")
public class APIActivityPushLogController {

  private final ApiActivityPushService apiActivityPushService;

  @Operation(summary = "Store api activity log info")
  @PostMapping
  public Mono<HttpResponse<ApiRequestActivityPushResult>> createPushApiActivityLogInfo(
      @RequestBody CreatePushApiActivityRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> apiActivityPushService.createPushApiActivityLogInfo(request, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "search push history")
  @GetMapping("/history")
  public HttpResponse<Paging<PushApiActivityLogHistory>> searchHistory(
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size,
      @RequestParam(value = "requestStartTime", required = false) ZonedDateTime requestStartTime,
      @RequestParam(value = "requestEndTime", required = false) ZonedDateTime requestEndTime) {
    return HttpResponse.ok(
        this.apiActivityPushService.searchHistory(
            PushLogSearchRequest.builder()
                .queryStart(requestStartTime)
                .queryEnd(requestEndTime)
                .build(),
            getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "check if push log enabled")
  @GetMapping("/enabled")
  public HttpResponse<PushApiActivityLogEnabled> isPushApiActivityLogEnabled() {
    return HttpResponse.ok(this.apiActivityPushService.isPushApiActivityLogEnabled());
  }
}
