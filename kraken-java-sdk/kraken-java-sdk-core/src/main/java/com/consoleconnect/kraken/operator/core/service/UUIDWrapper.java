package com.consoleconnect.kraken.operator.core.service;

import java.util.Optional;
import java.util.UUID;

public interface UUIDWrapper {
  default Optional<UUID> getUUID(String id) {
    try {
      return Optional.of(UUID.fromString(id));
    } catch (Exception e) {
      return Optional.empty();
    }
  }
}
