package com.consoleconnect.kraken.operator.gateway.dto;

import com.consoleconnect.kraken.operator.core.dto.AbstractHttpModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class HttpRequest extends AbstractHttpModel {

  private String bizType;
  private String externalId;
}
