package com.consoleconnect.kraken.operator.sync.service;

import static com.consoleconnect.kraken.operator.sync.ClientCredentialMockServerTest.ACCESS_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;

import com.consoleconnect.kraken.operator.core.enums.EventStatusType;
import com.consoleconnect.kraken.operator.core.enums.MgmtEventType;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.UnifiedAssetService;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.sync.MockServerTest;
import com.consoleconnect.kraken.operator.sync.model.MgmtEvent;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Mono;

class ResetServiceTest extends MockServerTest {

  @Autowired private ExternalSystemTokenProvider externalSystemTokenProvider;

  @Test
  void givenEventRecord_thenClearData() {
    final ResetService service =
        new ResetService(
            syncProperty,
            mockServer.getWebClient(),
            externalSystemTokenProvider,
            Mockito.mock(UnifiedAssetService.class));
    MgmtEvent event = new MgmtEvent();
    event.setEventType(MgmtEventType.RESET);
    event.setResourceId("mef.sonata.api-target-mapper.order.uni.add");
    event.setId("id");
    event.setStatus(EventStatusType.ACK);
    List<MgmtEvent> list = new ArrayList<>();
    list.add(event);
    Paging<MgmtEvent> paging = new Paging<>();
    paging.setData(list);
    paging.setTotal(1L);
    ResetService spy = Mockito.spy(service);
    doNothing().when(spy).updateEventStatus(anyList(), any(EventStatusType.class));
    mockServer
        .responseWith(HttpStatus.OK, HttpResponse.ok(paging), new HashMap<>())
        .call(
            () -> {
              spy.scanEvent();
              return Mono.empty();
            })
        .expectNoContent()
        .takeRequest()
        .expectHeader("Authorization", ACCESS_TOKEN)
        .expectMethod("GET")
        .expectPath("/v2/callback/event?page=0&size=100&status=ACK");
  }

  @Test
  void givenEvent_thenUpdateSuccess() {
    final ResetService service =
        new ResetService(
            syncProperty,
            mockServer.getWebClient(),
            externalSystemTokenProvider,
            Mockito.mock(UnifiedAssetService.class));
    MgmtEvent event = new MgmtEvent();
    event.setId("123");
    mockServer
        .responseWith(HttpStatus.OK, HttpResponse.ok(null), new HashMap<>())
        .call(
            () -> {
              service.updateEventStatus(List.of(event), EventStatusType.FAILED);
              return Mono.empty();
            })
        .expectNoContent()
        .takeRequest()
        .expectHeader("Authorization", ACCESS_TOKEN)
        .expectMethod("PATCH")
        .expectPath("/v2/callback/event");
  }
}
