package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.dto.UnifiedAssetDto;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;

@Data
public class EndPointUsageDTO {
  private Map<String, List<UnifiedAssetDto>> endpointUsage = new ConcurrentHashMap<>();
}
