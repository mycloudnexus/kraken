package com.consoleconnect.kraken.operator.controller.api.v2;

import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.controller.entity.MgmtEventEntity;
import com.consoleconnect.kraken.operator.controller.service.ComponentLoadService;
import com.consoleconnect.kraken.operator.controller.service.EventService;
import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

@ConditionalOnExpression(
    "'${app.unified-asset.endpoints.exposure.include}'.contains('component-mapper')")
@AllArgsConstructor
@RestController("componentMapperLoadV2")
@RequestMapping(value = "/v2/components/mappers", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Mapper load V2", description = "Mapper load V2")
@Slf4j
public class ComponentLoadController {
  private final EventService eventService;
  private final ComponentLoadService componentLoadService;

  @Operation(summary = "reset mapper")
  @PostMapping("/reset/{key}")
  public HttpResponse<String> resetMapper(
      @PathVariable("key") String key, ServerWebExchange exchange) {
    Paging<MgmtEventEntity> search =
        eventService.search(
            MgmtEventType.RESET.name(), EventStatusType.ACK.name(), PageRequest.of(0, 10));

    if (search != null && search.getTotal() > 0) {
      throw KrakenException.forbidden(
          "There is an event already in Processing, please wait until finished");
    }
    String userId =
        (String) exchange.getAttributes().get(new AuthDataProperty.ResourceServer().getUserId());
    MgmtEventEntity entity = eventService.publishEvent(MgmtEventType.RESET, userId, key);
    return HttpResponse.ok(entity.getId().toString());
  }

  @GetMapping("/load/{key}")
  @Operation(summary = "load mapper with sample data")
  public HttpResponse<UnifiedAssetDto> loadMapperData(@PathVariable("key") String key) {
    return HttpResponse.ok(componentLoadService.loadMapperData(key));
  }
}
