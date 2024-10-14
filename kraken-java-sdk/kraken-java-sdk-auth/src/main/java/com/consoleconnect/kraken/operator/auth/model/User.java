package com.consoleconnect.kraken.operator.auth.model;

import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import com.consoleconnect.kraken.operator.core.model.AbstractModel;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class User extends AbstractModel {
  private String name;
  private String email;
  private String role;
  private UserStateEnum state;

  private List<UserToken> tokens;
}
