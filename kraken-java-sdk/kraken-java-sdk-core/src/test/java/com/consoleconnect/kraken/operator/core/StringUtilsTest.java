package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.enums.RegisterActionTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.TaskEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum;
import com.consoleconnect.kraken.operator.core.toolkit.StringUtils;
import java.util.UUID;
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
    String shortId = StringUtils.shortenUUID(UUID.randomUUID().toString());
    RegisterActionTypeEnum type = RegisterActionTypeEnum.fromString("REGISTER");
    Assertions.assertNotNull(type);
    Assertions.assertNotNull(WorkflowStageEnum.EXECUTION_STAGE);
    Assertions.assertNotNull(WorkflowStageEnum.PREPARATION_STAGE);
    Assertions.assertNotNull(WorkflowStageEnum.VALIDATION_STAGE);
    Assertions.assertNotNull(TaskEnum.HTTP);
    Assertions.assertNotNull(TaskEnum.SWITCH);
    Assertions.assertEquals(8, shortId.length());
  }
}
