package com.consoleconnect.kraken.operator.controller.handler;

import com.consoleconnect.kraken.operator.core.client.ClientEvent;
import com.consoleconnect.kraken.operator.core.client.ClientEventTypeEnum;
import com.consoleconnect.kraken.operator.core.model.HttpResponse;
import com.consoleconnect.kraken.operator.core.service.ApiActivityLogService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ClientAPIAuditLogEventHandler extends ClientEventHandler {

  private final ApiActivityLogService apiActivityLogService;

  @Override
  @Transactional
  public HttpResponse<Void> onEvent(String envId, String userId, ClientEvent event) {
    return this.apiActivityLogService.onEvent(envId, userId, event);
  }

  @Override
  public ClientEventTypeEnum getEventType() {
    return ClientEventTypeEnum.CLIENT_API_AUDIT_LOG;
  }
}
