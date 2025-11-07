package com.consoleconnect.kraken.operator.auth.dto;

import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ValidationErrorDto {

  private String message;

  private Map<String, String> fieldErrors;
}
