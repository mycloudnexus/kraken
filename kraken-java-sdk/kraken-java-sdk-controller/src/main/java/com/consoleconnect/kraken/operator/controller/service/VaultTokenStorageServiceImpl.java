package com.consoleconnect.kraken.operator.controller.service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VaultTokenStorageServiceImpl implements TokenStorageService {
  @Override
  public void writeSecret(String key, String value) {
    // 123
  }

  @Override
  public String readSecret(String key) {
    log.info("vault storage");
    return null;
  }
}
