package com.consoleconnect.kraken.operator.controller.service;

public interface TokenStorageService {
  void writeSecret(String key, String value);

  String readSecret(String key);
}
