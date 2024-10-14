package com.consoleconnect.kraken.operator.core.controller;

import com.consoleconnect.kraken.operator.core.enums.APISpecEnum;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@ConditionalOnExpression("'${app.unified-asset.endpoints.exposure.include}'.contains('component')")
@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/products/{productId}/components/{componentId}",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Unified Model Mgmt", description = "Unified Model Mgmt APIs")
@Slf4j
public class ComponentAPISpecController {

  private final AssetController service;

  @GetMapping("/api-docs")
  public byte[] getComponentApiDoc(
      @PathVariable("productId") String productId,
      @PathVariable("componentId") String componentId,
      @RequestParam(value = "specType", required = false) APISpecEnum specType) {
    log.info(
        "getComponentApiDoc: productId={}, componentId={}, specType={}",
        productId,
        componentId,
        specType);
    return service.downloadAPIDoc(componentId, specType);
  }
}
