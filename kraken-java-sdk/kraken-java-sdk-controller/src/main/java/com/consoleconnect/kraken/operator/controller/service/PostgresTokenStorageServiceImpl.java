package com.consoleconnect.kraken.operator.controller.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresTokenStorageServiceImpl implements TokenStorageService {
  @Override
  public void writeSecret(String key, String value) {}

  @Override
  public String readSecret(String key) {
    log.info("postgres storage");
    return null;
  }
}
