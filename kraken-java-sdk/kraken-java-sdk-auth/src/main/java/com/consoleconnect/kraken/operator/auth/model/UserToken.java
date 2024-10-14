package com.consoleconnect.kraken.operator.auth.model;

import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import java.util.Map;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserToken extends AbstractModel {
  private String name;
  private String userId;
  private long expiresAt;
  private long maxLifeEndAt;
  private boolean revoked;
  private String revokedBy;
  private String revokedAt;
  private String token;
  private UserTokenTypeEnum tokenType;
  private Map<String, Object> metadata;
  private Map<String, Object> claims;
}
