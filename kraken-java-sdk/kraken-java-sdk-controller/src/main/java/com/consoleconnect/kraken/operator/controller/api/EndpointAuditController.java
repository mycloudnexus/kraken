package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.controller.audit.EndpointAuditEntity;
import com.consoleconnect.kraken.operator.controller.audit.EndpointAuditService;
import com.consoleconnect.kraken.operator.controller.model.EndpointAudit;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/audit/logs", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Audit APIs", description = "Core APIs")
public class EndpointAuditController {

  private final EndpointAuditService service;

  public EndpointAuditController(EndpointAuditService service) {
    this.service = service;
  }

  @Operation(summary = "Search audit logs")
  @GetMapping()
  public HttpResponse<Paging<EndpointAuditEntity>> search(
      @RequestParam(value = "resource", required = false) String resource,
      @RequestParam(value = "resourceId", required = false) String resourceId,
      @RequestParam(value = "action", required = false) String action,
      @RequestParam(value = "userId", required = false) String userId,
      @RequestParam(value = "requestStartTime", required = false) Instant requestStartTime,
      @RequestParam(value = "requestEndTime", required = false) Instant requestEndTime,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    ZonedDateTime startTime =
        requestStartTime == null
            ? null
            : ZonedDateTime.ofInstant(requestStartTime, ZoneId.systemDefault());
    ZonedDateTime endTime =
        requestEndTime == null
            ? null
            : ZonedDateTime.ofInstant(requestEndTime, ZoneId.systemDefault());
    EndpointAudit query = new EndpointAudit();
    query.setResource(resource);
    query.setResourceId(resourceId);
    query.setAction(action);
    query.setUserId(userId);
    return HttpResponse.ok(this.service.search(query, startTime, endTime, page, size));
  }

  @Operation(summary = "Search a resource's audit logs")
  @GetMapping("/resources/{resourceId}")
  public HttpResponse<Paging<EndpointAuditEntity>> searchByResourceId(
      @PathVariable String resourceId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(this.service.searchByResourceId(resourceId, page, size));
  }
}