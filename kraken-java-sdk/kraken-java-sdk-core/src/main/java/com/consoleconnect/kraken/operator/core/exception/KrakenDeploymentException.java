package com.consoleconnect.kraken.operator.core.exception;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import lombok.Getter;

public class KrakenDeploymentException extends KrakenException {

  @Getter private ErrorSeverityEnum severity;

  @Getter private String component;

  @Getter private DeployComponentError error;

  public KrakenDeploymentException(ErrorSeverityEnum severity, String component, int code) {
    super(code);
    buildStatus(severity, component);
  }

  public KrakenDeploymentException(
      ErrorSeverityEnum severity, String component, int code, String message) {
    super(code, message);
    buildStatus(severity, component);
  }

  public static KrakenDeploymentException internalFatalError(String component, String message) {
    return new KrakenDeploymentException(ErrorSeverityEnum.FATAL, component, 500, message);
  }

  private void buildStatus(ErrorSeverityEnum severity, String component) {
    this.error =
        DeployComponentError.builder()
            .severity(severity)
            .component(component)
            .reason(getMessage())
            .build();
  }
}
