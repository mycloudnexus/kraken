package com.consoleconnect.kraken.operator.controller.api;

import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.controller.dto.CreateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.dto.UpdateSellerContactRequest;
import com.consoleconnect.kraken.operator.controller.service.SellerContactService;
import com.consoleconnect.kraken.operator.core.event.IngestionDataResult;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@AllArgsConstructor
@RestController()
@RequestMapping(
    value = "/products/{productId}/components/{componentId}/seller-contacts",
    produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Seller Contacts Mgmt", description = "Seller Contacts Mgmt APIs")
@Slf4j
public class SellerContactController {

  private final SellerContactService sellerContactService;

  @Operation(summary = "Create a seller contact")
  @PostMapping()
  public Mono<HttpResponse<List<IngestionDataResult>>> create(
      @PathVariable("productId") String productId,
      @PathVariable("componentId") String componentId,
      @RequestBody CreateSellerContactRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(
            userId ->
                sellerContactService.createSellerContact(productId, componentId, request, userId))
        .map(HttpResponse::ok);
  }

  @Operation(summary = "Update a seller contact by key or id")
  @PatchMapping()
  public Mono<HttpResponse<IngestionDataResult>> updateOne(
      @PathVariable("productId") String productId,
      @PathVariable("componentId") String componentId,
      @RequestBody UpdateSellerContactRequest request) {
    return UserContext.getUserId()
        .publishOn(Schedulers.boundedElastic())
        .map(userId -> sellerContactService.updateOne(productId, componentId, request, userId))
        .map(HttpResponse::ok);
  }
}
