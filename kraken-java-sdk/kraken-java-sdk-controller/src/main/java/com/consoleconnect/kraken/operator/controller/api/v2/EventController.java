package com.consoleconnect.kraken.operator.controller.api.v2;

import static com.consoleconnect.kraken.operator.core.service.UnifiedAssetService.getSearchPageRequest;

import com.consoleconnect.kraken.operator.controller.service.EventService;
import com.consoleconnect.kraken.operator.core.dto.UpdateStatusDto;
import com.consoleconnect.kraken.operator.core.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController("event")
@RequestMapping(value = "/v2/callback/event", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Event V2", description = "Event V2")
@Slf4j
public class EventController {

  private final EventService service;

  @Operation(summary = "list event")
  @GetMapping()
  public HttpResponse<Paging<MgmtEventEntity>> search(
      @RequestParam(value = "eventType", required = false, defaultValue = "RESET") String eventType,
      @RequestParam(value = "status", required = false, defaultValue = "ACK") String status,
      @RequestParam(value = "orderBy", required = false, defaultValue = "createdAt") String orderBy,
      @RequestParam(value = "direction", required = false, defaultValue = "DESC")
          Sort.Direction direction,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {

    return HttpResponse.ok(
        this.service.search(
            eventType, status, getSearchPageRequest(page, size, direction, orderBy)));
  }

  @Operation(summary = "Update event status")
  @PatchMapping()
  public HttpResponse<Void> updateBatch(@RequestBody UpdateStatusDto dto) {
    this.service.updateStatus(dto.getIds(), dto.getStatus());
    return HttpResponse.ok(null);
  }
}
