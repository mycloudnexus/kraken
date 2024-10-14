package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.core.dto.SimpleApiServerDto;
import lombok.Data;

@Data
public class APIServerEnvDTO extends SimpleApiServerDto {
  private String envName;
}
