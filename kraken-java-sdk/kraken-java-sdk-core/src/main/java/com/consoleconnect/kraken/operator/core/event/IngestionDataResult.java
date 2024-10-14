package com.consoleconnect.kraken.operator.core.event;

import com.consoleconnect.kraken.operator.core.entity.UnifiedAssetEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionDataResult {
  private int code;
  private String message;

  private UnifiedAssetEntity data;

  public static IngestionDataResult of(int code, String message) {
    return new IngestionDataResult(code, message, null);
  }

  public static IngestionDataResult of(int code, String message, UnifiedAssetEntity data) {
    return new IngestionDataResult(code, message, data);
  }
}
