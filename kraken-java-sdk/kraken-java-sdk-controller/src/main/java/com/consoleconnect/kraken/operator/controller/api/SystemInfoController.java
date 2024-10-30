package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.controller.enums.SystemStateEnum;
import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.service.SystemInfoService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "System Info", description = "System Info")
@Slf4j
public class SystemInfoController {
  private final SystemInfoService systemInfoService;

  @Operation(summary = "current system info")
  @GetMapping("system-info")
  public HttpResponse<SystemInfo> systemInfo() {
    return HttpResponse.ok(systemInfoService.find());
  }

  @Operation(summary = "update system status")
  @PostMapping("/system-status")
  public HttpResponse<SystemInfo> updateStatus(@RequestBody SystemInfo systemInfo) {
    systemInfoService.updateSystemStatus(SystemStateEnum.valueOf(systemInfo.getStatus()));
    return HttpResponse.ok(systemInfoService.find());
  }
}
