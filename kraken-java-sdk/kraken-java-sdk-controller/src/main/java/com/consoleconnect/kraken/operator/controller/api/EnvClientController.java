package com.consoleconnect.kraken.operator.controller.api;

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
@RequestMapping(value = "/products/{productId}/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Env Client Mgmt ", description = "Env Client Mgmt")
public class EnvClientController {
  private final EnvClientService envClientService;

  @Operation(summary = "List all of data plane client connected to the control plane")
  @GetMapping("/envs/{envId}/clients")
  public HttpResponse<Paging<EnvironmentClient>> listClients(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        envClientService.listClients(
            envId, ClientReportTypeEnum.HEARTBEAT, PageRequest.of(page, size)));
  }

  @Operation(
      summary = "List all of data plane client connected to the control plane for all environments")
  @GetMapping("/env-clients")
  public HttpResponse<Paging<EnvironmentClient>> listClientsWithAllEnv(
      @PathVariable("productId") String productId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        envClientService.listClients(
            null, ClientReportTypeEnum.HEARTBEAT, PageRequest.of(page, size)));
  }

  @Operation(summary = "List clients that have report reload operation info to the control plane")
  @GetMapping("envs/{envId}/clients/configuration-reload")
  public HttpResponse<Paging<EnvironmentClient>> listClientsReload(
      @PathVariable("productId") String productId,
      @PathVariable("envId") String envId,
      @RequestParam(value = "page", required = false, defaultValue = PagingHelper.DEFAULT_PAGE_STR)
          int page,
      @RequestParam(value = "size", required = false, defaultValue = PagingHelper.DEFAULT_SIZE_STR)
          int size) {
    return HttpResponse.ok(
        envClientService.listClients4Reload(
            envId, ClientReportTypeEnum.DEPLOY, PageRequest.of(page, size)));
  }
}
