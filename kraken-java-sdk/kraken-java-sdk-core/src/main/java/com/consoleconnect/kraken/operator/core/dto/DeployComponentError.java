package com.consoleconnect.kraken.operator.core.dto;

import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import com.consoleconnect.kraken.operator.core.exception.KrakenDeploymentException;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeployComponentError {

  private ErrorSeverityEnum severity;

  private String assetKind;

  private String assetId;

  private String reason;

  public static DeployComponentError of(UnifiedAssetDto assetDto, KrakenDeploymentException e) {
    return DeployComponentError.builder()
        .severity(e.getError().getSeverity())
        .assetKind(assetDto.getKind())
        .assetId(assetDto.getId())
        .reason(e.getMessage())
        .build();
  }

  public static DeployComponentError of(UnifiedAssetDto assetDto, Exception e) {
    return DeployComponentError.builder()
        .severity(ErrorSeverityEnum.ERROR)
        .assetKind(assetDto.getKind())
        .assetId(assetDto.getId())
        .reason(e.getMessage())
        .build();
  }
}
