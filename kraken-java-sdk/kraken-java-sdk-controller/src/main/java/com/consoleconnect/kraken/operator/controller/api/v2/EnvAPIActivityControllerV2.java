package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.core.dto.ApiActivityLog;
import com.consoleconnect.kraken.operator.core.dto.ComposedHttpRequest;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.request.LogSearchRequest;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/envs/{envId}/api-activities",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "API Activities ", description = "API Activities")
public class EnvAPIActivityControllerV2 {
  private final ApiActivityLogService apiActivityLogService;

  @Operation(summary = "search activities")
  @GetMapping()
  public HttpResponse<Paging<ApiActivityLog>> search(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "requestId", required = false) String requestId,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size,
      @RequestParam(value = "requestStartTime", required = false) Instant requestStartTime,
      @RequestParam(value = "requestEndTime", required = false) Instant requestEndTime,
      @RequestParam(value = "productType", required = false) String productType,
      @RequestParam(value = "path", required = false) String path,
      @RequestParam(value = "methods", required = false) List<String> methods,
      @RequestParam(value = "statusCodes", required = false) List<Integer> statusCodes) {
    ZonedDateTime startTime =
        requestStartTime == null
            ? null
            : ZonedDateTime.ofInstant(requestStartTime, ZoneId.systemDefault());
    ZonedDateTime endTime = null;
    endTime =
        requestEndTime == null
            ? null
            : ZonedDateTime.ofInstant(requestEndTime, ZoneId.systemDefault());
    return HttpResponse.ok(
        this.apiActivityLogService.search(
            LogSearchRequest.builder()
                .env(envId)
                .requestId(requestId)
                .methods(methods)
                .path(path)
                .queryStart(startTime)
                .queryEnd(endTime)
                .productType(productType)
                .statusCodes(statusCodes)
                .build(),
            getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Retrieve a activity details")
  @GetMapping("{activityId}")
  public HttpResponse<ComposedHttpRequest> findOne(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @PathVariable("activityId") String activityId) {
    Optional<ComposedHttpRequest> detail = apiActivityLogService.getDetail(activityId);
    if (detail.isPresent()) {
      ComposedHttpRequest composedHttpRequest = detail.get();
      return HttpResponse.ok(composedHttpRequest);
    }
    return HttpResponse.ok(null);
  }
}
