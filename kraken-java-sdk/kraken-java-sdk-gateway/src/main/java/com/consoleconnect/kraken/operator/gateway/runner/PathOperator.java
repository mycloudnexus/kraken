package com.consoleconnect.kraken.operator.gateway.runner;

import com.jayway.jsonpath.DocumentContext;
import lombok.extern.slf4j.Slf4j;

public interface PathOperator {

  @Slf4j
  final class LogHolder {}

  default void deleteByPath(String path, DocumentContext doc) {
    try {
      doc.delete(path);
    } catch (Exception e) {
      LogHolder.log.warn("Delete path {} error: {}", path, e.getMessage());
    }
  }
}
