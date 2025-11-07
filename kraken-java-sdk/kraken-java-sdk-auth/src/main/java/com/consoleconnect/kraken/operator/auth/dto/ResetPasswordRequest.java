package com.consoleconnect.kraken.operator.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordRequest {

  @NotNull
  @Pattern(
      regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[\\W_])(?=.{12,})(?!.*\\s).+$",
      message =
          """
        - at least one digit
        - at least one lowercase letter
        - at least one uppercase letter
        - at least one special character
        - none whitespace
        - at least 12 characters long.
      """)
  private String password;
}
