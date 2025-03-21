package com.consoleconnect.kraken.operator.core.exception;

import com.consoleconnect.kraken.operator.core.enums.ErrorSeverityEnum;
import lombok.Getter;

public class KrakenDeploymentException extends KrakenException {

  @Getter private final ErrorSeverityEnum severity;

  public KrakenDeploymentException(ErrorSeverityEnum severity, int code) {
    super(code);
    this.severity = severity;
  }

  public KrakenDeploymentException(ErrorSeverityEnum severity, int code, String message) {
    super(code, message);
    this.severity = severity;
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
}
