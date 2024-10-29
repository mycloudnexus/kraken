package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.enums.RegisterActionTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void testEnum() {
    RegisterActionTypeEnum register = RegisterActionTypeEnum.REGISTER;
    RegisterActionTypeEnum unregister = RegisterActionTypeEnum.UNREGISTER;
    RegisterActionTypeEnum read = RegisterActionTypeEnum.READ;
    System.out.println(register.name());
    System.out.println(unregister.name());
    System.out.println(read.name());
    RegisterActionTypeEnum type = RegisterActionTypeEnum.fromString("REGISTER");
    Assertions.assertNotNull(type);
  }
}
