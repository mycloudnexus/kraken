package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.enums.RegisterActionTypeEnum;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringUtilsTest {

  @Test
  void testDataTimeStr() {
    Assertions.assertNotNull(DateTime.nowInUTCFormatted());
  }

  @Test
  void testTruncate() {
    String raw2 = "123456    ";
    String res2 = StringUtils.truncate(raw2, 3);
    Assertions.assertEquals(3, res2.length());
  }

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
