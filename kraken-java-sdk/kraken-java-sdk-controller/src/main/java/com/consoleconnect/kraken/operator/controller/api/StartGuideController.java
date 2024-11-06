package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.controller.dto.start.StartGuideInfoDto;
import com.consoleconnect.kraken.operator.controller.service.start.StartGuideService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = StartGuideController.URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "Start Guide APIs", description = "Portal APIs")
public class StartGuideController {

  public static final String URL = "/start/guide";
  private final StartGuideService service;

  @Operation(summary = "Get start guide info")
  @GetMapping("/{productId}")
  public HttpResponse<StartGuideInfoDto> getStartGuideInfo(
      @PathVariable("productId") String productId, @RequestParam(value = "kind") String kind) {
    return HttpResponse.ok(service.getStartGuideInfo(productId, kind));
  }
}
