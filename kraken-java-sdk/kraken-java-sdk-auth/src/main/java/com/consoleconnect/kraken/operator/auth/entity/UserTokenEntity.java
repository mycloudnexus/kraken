package com.consoleconnect.kraken.operator.auth.entity;

import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Type;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_user_token",
    indexes = {
      @Index(name = "kraken_user_token_idx_userId", columnList = "user_id"),
      @Index(name = "kraken_user_token_idx_token", columnList = "token", unique = true),
      @Index(name = "kraken_user_token_idx_userId_tokenType", columnList = "user_id,token_type"),
      @Index(
          name = "kraken_user_token_idx_userId_tokenType_revoked",
          columnList = "user_id,token_type,revoked"),
      @Index(
          name = "kraken_user_token_idx_userId_tokenType_expired",
          columnList = "user_id,token_type,revoked,expires_at,max_life_end_at")
    })
public class UserTokenEntity extends AbstractEntity {

  @Column(name = "user_id", nullable = false, unique = false)
  private String userId;

  @Column(name = "name", nullable = false, unique = false)
  private String name;

  @Column(name = "token", nullable = false, unique = true)
  private String token;

  @Enumerated(EnumType.STRING)
  @Column(name = "token_type", nullable = false, unique = false)
  private UserTokenTypeEnum tokenType;

  @Column(name = "expires_at", nullable = true, unique = false)
  private long expiresAt;

  @Column(name = "max_life_end_at", nullable = true, unique = false)
  private long maxLifeEndAt;

  @Column(name = "revoked", nullable = false, unique = false)
  private boolean revoked;

  @Column(name = "revoked_at", nullable = true, unique = false)
  private ZonedDateTime revokedAt;

  @Column(name = "revoked_by", nullable = true, unique = false)
  private String revokedBy;

  @Type(JsonType.class)
  @Column(name = "claims", columnDefinition = "jsonb")
  private Map<String, Object> claims = new HashMap<>();

  @Type(JsonType.class)
  @Column(name = "metadata", columnDefinition = "jsonb")
  private Map<String, Object> metadata = new HashMap<>();
}
