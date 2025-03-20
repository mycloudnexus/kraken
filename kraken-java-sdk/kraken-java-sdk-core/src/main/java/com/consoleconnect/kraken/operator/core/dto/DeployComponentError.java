package com.consoleconnect.kraken.operator.core.dto;

import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeployComponentError {

  private ErrorSeverityEnum severity;

  private String component;

  private String reason;

  public static DeployComponentError of(Exception e) {
    return DeployComponentError.builder()
        .severity(ErrorSeverityEnum.ERROR)
        .component("")
        .reason(e.getMessage())
        .build();
  }
}
