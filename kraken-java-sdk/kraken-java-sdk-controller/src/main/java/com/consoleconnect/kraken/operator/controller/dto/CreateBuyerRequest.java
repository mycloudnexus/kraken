package com.consoleconnect.kraken.operator.controller.dto;

import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import lombok.Data;

@Data
public class CreateBuyerRequest {
  private String buyerId;
  private String companyName;
  private String envId;

  private long tokenExpiredInSeconds;
  private String role = UserRoleEnum.USER.name();
}
