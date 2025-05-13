package com.consoleconnect.kraken.operator.gateway.runner;

import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogBodyEntity;
import com.consoleconnect.kraken.operator.core.entity.ApiActivityLogEntity;
import com.consoleconnect.kraken.operator.core.repo.ApiActivityLogRepository;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.gateway.CustomConfig;
import com.consoleconnect.kraken.operator.gateway.dto.RoutingResultDto;
import com.consoleconnect.kraken.operator.gateway.filter.KrakenFilterConstants;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.UUID;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.server.ServerWebExchange;

@MockIntegrationTest
@ContextConfiguration(classes = CustomConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PatternActionRunnerTest extends AbstractIntegrationTest {

  @Autowired PatternActionRunner patternActionRunner;
  @Autowired private ApiActivityLogService apiActivityLogService;
  @Autowired private ApiActivityLogRepository apiActivityLogRepository;

  @SneakyThrows
  @Test
  void givenRequestWithProductType_whenRunningAction_thenProductTypeSavedOK() {
    String resultInJson = readFileToString("/mockData/routing-ok.json");
    ApiActivityLogBodyEntity apiLogBodyEntity = new ApiActivityLogBodyEntity();
    RoutingResultDto routingResultDto = JsonToolkit.fromJson(resultInJson, RoutingResultDto.class);
    routingResultDto.setProductType("SHARE");
    String request = JsonToolkit.toJson(routingResultDto);
    apiLogBodyEntity.setRequest(request);

    ApiActivityLogEntity entity = new ApiActivityLogEntity();
    entity.setRequestId(UUID.randomUUID().toString());
    entity.setPath("/mefApi/sonata/quoteManagement/v8/quote");
    entity.setMethod("POST");
    entity.setUri("localhost");
    entity.setApiLogBodyEntity(apiLogBodyEntity);
    entity.setCallSeq(0);
    entity.setRequest(request);
    entity = apiActivityLogService.save(entity);

    ServerWebExchange exchange = Mockito.mock(ServerWebExchange.class);
    Mockito.doReturn(entity.getId().toString())
        .when(exchange)
        .getAttribute(KrakenFilterConstants.X_LOG_ENTITY_ID);
    patternActionRunner.recordProductType(exchange, routingResultDto);
    entity = apiActivityLogRepository.findById(entity.getId()).orElseThrow();
    String productType = entity.getProductType();
    Assertions.assertEquals("SHARE", productType);
  }
}
