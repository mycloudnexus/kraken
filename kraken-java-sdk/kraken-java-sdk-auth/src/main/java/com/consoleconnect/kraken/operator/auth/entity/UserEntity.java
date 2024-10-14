package com.consoleconnect.kraken.operator.auth.entity;

import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import com.consoleconnect.kraken.operator.core.entity.AbstractEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(
    name = "kraken_user",
    indexes = {@Index(name = "kraken_user_idx_email", columnList = "email")})
public class UserEntity extends AbstractEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "email", nullable = false, unique = true)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "role", nullable = false)
  private String role;

  @Enumerated(EnumType.STRING)
  @Column(name = "state", nullable = false)
  private UserStateEnum state;
}
