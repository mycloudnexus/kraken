package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FilterHeaderService {

  private final Set<String> filters;

  public FilterHeaderService(final AppProperty appProperty) {
    if (appProperty.getFilterHeaders() != null) {
      filters =
          appProperty.getFilterHeaders().stream()
              .map(String::toLowerCase)
              .collect(Collectors.toSet());
    } else {
      filters = Set.of();
    }
  }

  public Map<String, String> filterHeaders(Map<String, String> headers) {
    if (headers == null) {
      return Map.of();
    }
    Map<String, String> headerNew = new HashMap<>();
    headers.entrySet().stream()
        .filter(entry -> !filters.contains(entry.getKey().toLowerCase()))
        .forEach(entry -> headerNew.put(entry.getKey(), entry.getValue()));
    return headerNew;
  }
}
