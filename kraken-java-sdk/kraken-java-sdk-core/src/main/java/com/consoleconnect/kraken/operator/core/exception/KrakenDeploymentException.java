package com.consoleconnect.kraken.operator.core.exception;

import com.consoleconnect.kraken.operator.core.dto.DeployComponentError;
import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import lombok.Getter;

public class KrakenDeploymentException extends KrakenException {

  @Getter private DeployComponentError error;

  public KrakenDeploymentException(ErrorSeverityEnum severity, int code) {
    super(code);
    buildError(severity);
  }

  public KrakenDeploymentException(ErrorSeverityEnum severity, int code, String message) {
    super(code, message);
    buildError(severity);
  }

  public static KrakenDeploymentException internalFatalError(String message) {
    return new KrakenDeploymentException(ErrorSeverityEnum.FATAL, 500, message);
  }

  public static KrakenDeploymentException internalWarningError(String message) {
    return new KrakenDeploymentException(ErrorSeverityEnum.WARNING, 500, message);
  }

  public static KrakenDeploymentException internalNoticeError(String message) {
    return new KrakenDeploymentException(ErrorSeverityEnum.WARNING, 500, message);
  }

  public static KrakenDeploymentException internalNoticeError() {
    return new KrakenDeploymentException(ErrorSeverityEnum.WARNING, 500);
  }

  private void buildError(ErrorSeverityEnum severity) {
    this.error = DeployComponentError.builder().severity(severity).reason(getMessage()).build();
  }
}
