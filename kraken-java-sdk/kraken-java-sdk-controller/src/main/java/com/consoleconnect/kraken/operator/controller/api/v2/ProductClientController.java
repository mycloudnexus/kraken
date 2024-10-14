package com.consoleconnect.kraken.operator.controller.api.v2;

import com.consoleconnect.kraken.operator.controller.model.EnvironmentClient;
import com.consoleconnect.kraken.operator.controller.service.EnvClientService;
import com.consoleconnect.kraken.operator.core.enums.ClientReportTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/v2/products/{productId}/clients",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Product V2", description = "Product V2")
public class ProductClientController {
  private final EnvClientService envClientService;

  @Operation(summary = "List all of connected clients")
  @GetMapping()
  public HttpResponse<Paging<EnvironmentClient>> listClients(
      @PathVariable("productId") String productId,
      @RequestParam(value = "envId", required = false) String envId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        envClientService.listClients(
            envId, ClientReportTypeEnum.HEARTBEAT, PageRequest.of(page, size)));
  }
}
