package com.consoleconnect.kraken.operator.auth.dto;

import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import java.util.Map;
import lombok.Data;

@Data
public class CreateUserTokenRequest {
  private String userId;
  private String name;
  private Map<String, Object> claims;
  private Map<String, Object> metadata;
  private long expiresInSeconds;
  private long maxLifeEndInSeconds;

  private String token;
  private UserTokenTypeEnum tokenType = UserTokenTypeEnum.REFRESH_TOKEN;
}
