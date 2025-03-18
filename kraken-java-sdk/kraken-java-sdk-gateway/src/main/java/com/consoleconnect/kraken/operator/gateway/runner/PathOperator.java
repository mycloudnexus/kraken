package com.consoleconnect.kraken.operator.gateway.runner;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.COMMA;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.COMMA_SPACE_EXPRESSION;

import com.jayway.jsonpath.DocumentContext;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;

public interface PathOperator {

  @Slf4j
  final class LogHolder {}

  default void deleteByPath(String path, DocumentContext doc) {
    try {
      if (path.contains(COMMA)) {
        Arrays.stream(path.split(COMMA_SPACE_EXPRESSION)).forEach(doc::delete);
      } else {
        doc.delete(path);
      }
    } catch (Exception e) {
      LogHolder.log.warn("Delete path {} error: {}", path, e.getMessage());
    }
  }
}
