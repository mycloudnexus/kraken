package com.consoleconnect.kraken.operator.auth.dto;

import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateUserRequest {
  @NotNull private String name;

  @NotNull private String email;

  @NotNull
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+=])(?=\\S+$).{12,64}$",
      message =
          """
    - at least one digit
    - at least one lowercase letter
    - at least one uppercase letter
    - at least one special character
    - none whitespace
    - length be between 12 and 64 characters long.
  """)
  private String password;

  private String role = UserRoleEnum.USER.name();
}
