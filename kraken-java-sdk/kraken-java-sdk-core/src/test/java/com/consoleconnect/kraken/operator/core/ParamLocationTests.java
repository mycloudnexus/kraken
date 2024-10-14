package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.enums.ParamLocationEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ParamLocationTests {

  @Test
  void testCase() {
    Assertions.assertTrue(ParamLocationEnum.BODY.name().equalsIgnoreCase("BODY"));
    Assertions.assertTrue(ParamLocationEnum.PATH.name().equalsIgnoreCase("PATH"));
    Assertions.assertTrue(ParamLocationEnum.QUERY.name().equalsIgnoreCase("QUERY"));
    Assertions.assertTrue(ParamLocationEnum.HYBRID.name().equalsIgnoreCase("HYBRID"));
    Assertions.assertTrue(ParamLocationEnum.HEADER.name().equalsIgnoreCase("HEADER"));
  }
}
