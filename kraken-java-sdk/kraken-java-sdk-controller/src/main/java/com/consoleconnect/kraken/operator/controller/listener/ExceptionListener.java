package com.consoleconnect.kraken.operator.controller.listener;

import com.consoleconnect.kraken.operator.controller.audit.EndpointAuditRepository;
import com.consoleconnect.kraken.operator.core.event.ExceptionEvent;
import com.consoleconnect.kraken.operator.core.toolkit.AuditConstants;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ExceptionListener {
  private final EndpointAuditRepository repository;

  @EventListener(ExceptionEvent.class)
  @Async
  public void onException(ExceptionEvent event) {
    String id = (String) event.getExchange().getAttributes().get(AuditConstants.AUDIT_KEY);
    if (StringUtils.isBlank(id)) {
      return;
    }
    repository
        .findById(UUID.fromString(id))
        .ifPresent(
            entity -> {
              entity.setStatusCode(event.getCode());
              entity.setResponse(event.getException());
              repository.save(entity);
            });
  }
}
