package com.consoleconnect.kraken.operator.sync.service;

import com.consoleconnect.kraken.operator.sync.model.SyncProperty;
import com.consoleconnect.kraken.operator.sync.service.security.ExternalSystemTokenProvider;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
@ConditionalOnProperty(value = "app.control-plane.auth.auth-mode", havingValue = "internalToken")
@Slf4j
public class InternalTokenProvider implements ExternalSystemTokenProvider {

  private final SyncProperty appProperty;

  @Override
  public String getToken() {
    return ExternalSystemTokenProvider.BEARER_TOKEN_PREFIX
        + appProperty.getControlPlane().getAuth().getInternalToken().getAccessToken();
  }
}
