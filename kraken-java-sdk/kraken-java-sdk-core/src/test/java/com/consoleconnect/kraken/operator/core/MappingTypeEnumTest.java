package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.enums.MappingTypeEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MappingTypeEnumTest {

  @Test
  void givenMappingTypeEnum_whenCheckingDiscrete_thenReturnOK() {
    for (MappingTypeEnum type : MappingTypeEnum.values()) {
      Assertions.assertNotNull(type.getDiscrete());
    }
  }
}
