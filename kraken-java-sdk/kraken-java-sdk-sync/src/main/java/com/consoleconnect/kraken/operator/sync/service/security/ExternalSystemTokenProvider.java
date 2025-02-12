package com.consoleconnect.kraken.operator.sync.service.security;

public interface ExternalSystemTokenProvider {

  String BEARER_TOKEN_PREFIX = "Bearer ";

  String getToken();
}
