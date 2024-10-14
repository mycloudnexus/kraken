package com.consoleconnect.kraken.operator.gateway.service;

import com.consoleconnect.kraken.operator.core.model.AppProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class FilterHeaderService {
  private final AppProperty appProperty;

  public Map<String, String> filterHeaders(Map<String, String> headers) {
    List<String> filterHeaders = appProperty.getFilterHeaders();
    if (headers == null || filterHeaders == null) {
      return headers;
    }
    Map<String, String> headerNew = new HashMap<>();
    headers.entrySet().stream()
        .filter(entry -> !filterHeaders.contains(entry.getKey()))
        .forEach(entry -> headerNew.put(entry.getKey(), entry.getValue()));
    return headerNew;
  }
}
