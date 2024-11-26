package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.controller.service.KrakenVersionSpecificationBuildService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@RequestMapping(value = "/products/{productId}")
@Tag(
    name = "Kraken Version Specification Builder",
    description = "Kraken Version Specification Builder")
@Slf4j
@RequiredArgsConstructor
@Controller
public class KrakenApiUseCaseBuilderController {

  private final KrakenVersionSpecificationBuildService krakenVersionSpecificationBuildService;

  @SneakyThrows
  @Operation(summary = "generate version specification")
  @GetMapping("/version-specification")
  public ResponseEntity<Mono<Resource>> versionSpecification(@PathVariable String productId) {
    return krakenVersionSpecificationBuildService.buildKrakenVersionSpecification();
  }
}
