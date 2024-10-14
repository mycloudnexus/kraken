package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import lombok.Data;

@Data
public class CreateAPITokenRequest {
  private String name;
  private long tokenExpiresInSeconds;
  private String envId;
  private String role = UserRoleEnum.API_CLIENT.name();
  private String userId;
}
