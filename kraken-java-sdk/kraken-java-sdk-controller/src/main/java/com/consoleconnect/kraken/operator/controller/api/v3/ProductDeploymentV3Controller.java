package com.consoleconnect.kraken.operator.controller.api.v3;

import com.consoleconnect.kraken.operator.controller.dto.ApiMapperDeploymentDTO;
import com.consoleconnect.kraken.operator.controller.service.ProductDeploymentService;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@AllArgsConstructor
@RestController()
@RequestMapping(value = "/v3/products/{productId}/", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Template Upgrade V3", description = "Template Upgrade V3")
@Slf4j
public class ProductDeploymentV3Controller {
  private final ProductDeploymentService productDeploymentService;

  @Operation(summary = "list running api mapper deployments in the env")
  @GetMapping("running-api-mapper-deployments")
  public HttpResponse<List<ApiMapperDeploymentDTO>> retrieveApiMapperDeployment(
      @PathVariable String productId, @RequestParam String envId) {

    return HttpResponse.ok(productDeploymentService.listRunningApiMapperDeploymentV3(envId));
  }
}
