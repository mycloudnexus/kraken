package com.consoleconnect.kraken.operator.controller.model;

import com.consoleconnect.kraken.operator.auth.model.UserToken;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class APIToken extends UserToken {
  private String envId;
}
