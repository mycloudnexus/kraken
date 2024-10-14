package com.consoleconnect.kraken.operator.core.dto;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStatusDto {
  private List<String> ids;
  private String status;
}
