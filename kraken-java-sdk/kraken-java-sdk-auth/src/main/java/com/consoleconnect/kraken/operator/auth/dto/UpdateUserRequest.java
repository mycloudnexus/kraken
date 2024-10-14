package com.consoleconnect.kraken.operator.auth.dto;

import lombok.Data;

@Data
public class UpdateUserRequest {
  private String name;
  private String role;
  private String email;
}
