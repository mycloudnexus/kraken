package com.consoleconnect.kraken.operator.auth.dto;

import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateUserRequest {
  @NotNull private String name;
  @NotNull private String email;
  @NotNull private String password;
  private String role = UserRoleEnum.USER.name();
}
