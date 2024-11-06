package com.consoleconnect.kraken.operator.controller.api;

import static com.consoleconnect.kraken.operator.core.model.HttpResponse.ok;

import com.consoleconnect.kraken.operator.controller.dto.statistics.ApiRequestActivityStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.ErrorApiRequestStatistics;
import com.consoleconnect.kraken.operator.controller.dto.statistics.MostPopularEndpointStatistics;
import com.consoleconnect.kraken.operator.controller.service.statistics.ApiActivityStatisticsService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.request.ApiStatisticsSearchRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/products/{productId}/envs/{envId}/statistics",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "API Activities Statistics", description = "API Activities Statistics")
public class EnvAPIActivityStatisticsController {

  private final ApiActivityStatisticsService apiActivityStatisticsService;

  @Operation(summary = "Load api activity request statistics")
  @GetMapping("/api-activity-requests")
  public HttpResponse<ApiRequestActivityStatistics> getRequestStatistics(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "requestStartTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime requestStartTime,
      @RequestParam(value = "requestEndTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime requestEndTime,
      @RequestParam(value = "buyerId", required = false) String buyerId) {

    return ok(
        apiActivityStatisticsService.loadRequestStatistics(
            ApiStatisticsSearchRequest.builder()
                .env(envId)
                .buyerId(buyerId)
                .queryStart(requestStartTime)
                .queryEnd(requestEndTime)
                .build()));
  }

  @Operation(summary = "Load error request statistics")
  @GetMapping("/error-requests")
  public HttpResponse<ErrorApiRequestStatistics> getErrorStatistics(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "requestStartTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime requestStartTime,
      @RequestParam(value = "requestEndTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime requestEndTime,
      @RequestParam(value = "buyerId", required = false) String buyerId) {

    return ok(
        apiActivityStatisticsService.loadErrorsStatistics(
            ApiStatisticsSearchRequest.builder()
                .env(envId)
                .buyerId(buyerId)
                .queryStart(requestStartTime)
                .queryEnd(requestEndTime)
                .build()));
  }

  @Operation(summary = "Load most popular endpoint statistics")
  @GetMapping("/most-popular-endpoint")
  public HttpResponse<MostPopularEndpointStatistics> getMostPopularEndpointStatistics(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "requestStartTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime requestStartTime,
      @RequestParam(value = "requestEndTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          ZonedDateTime requestEndTime,
      @RequestParam(value = "buyerId", required = false) String buyerId) {

    return ok(
        apiActivityStatisticsService.loadMostPopularEndpointStatistics(
            ApiStatisticsSearchRequest.builder()
                .env(envId)
                .buyerId(buyerId)
                .queryStart(requestStartTime)
                .queryEnd(requestEndTime)
                .build()));
  }
}
