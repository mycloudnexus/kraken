package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.enums.RegisterActionTypeEnum;
import com.consoleconnect.kraken.operator.core.enums.TaskEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStageEnum;
import com.consoleconnect.kraken.operator.core.enums.WorkflowStatusEnum;
import com.consoleconnect.kraken.operator.core.exception.ErrorResponse;
import com.consoleconnect.kraken.operator.core.toolkit.StringUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class StringUtilsTest {
  private static final String EXPECTED_RAW =
      "{error:{message:The request/portId must match pattern \\^[a-f0-9]{24}$\\,status:400,statusCode:400}}";

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
    Assertions.assertNotNull(WorkflowStatusEnum.FAILED);
    Assertions.assertNotNull(WorkflowStatusEnum.IN_PROGRESS);
    Assertions.assertNotNull(WorkflowStatusEnum.SUCCESS);
    Assertions.assertEquals(8, shortId.length());
  }

  @Test
  void givenBlankRawMsg_whenTruncate_thenReturnOK() {
    String raw = "    ";
    String r1 = StringUtils.truncate(raw, 255);
    Assertions.assertEquals(raw, r1);
    String r2 = StringUtils.removeEscapedCharacter(raw, " ");
    Assertions.assertEquals(raw, r2);
  }

  @Test
  void givenRawMsg_whenTruncate_thenReturnOK() {
    String raw =
        "{\"error\":{\"message\":\"The request/portId must match pattern \\\"^[a-f0-9]{24}$\\\"\",\"status\":400,\"statusCode\":400}}";
    String result = StringUtils.truncate(raw, 255);
    Assertions.assertEquals(EXPECTED_RAW, result);
  }

  @Test
  void givenRawMsgWithOutAt_whenProcess_thenReturnOK() {
    String raw =
        "{\"error\":{\"message\":\"The request/portId must match pattern \\\"^[a-f0-9]{24}$\\\"\",\"status\":400,\"statusCode\":400}}";
    ErrorResponse errorResponse = new ErrorResponse();
    StringUtils.processRawMessage(errorResponse, raw);
    String r1 = errorResponse.getMessage();
    Assertions.assertEquals(EXPECTED_RAW, r1);
    raw = "   ";
    StringUtils.processRawMessage(errorResponse, raw);
    String r2 = errorResponse.getMessage();
    Assertions.assertEquals("", r2);
  }

  @Test
  void givenRawMsgWithAt_whenProcess_thenReturnOK() {
    String raw =
        "{\"error\":{\"message\":\"The @{{request/portId}} must match pattern \\\"^[a-f0-9]{24}$\\\"\",\"status\":400,\"statusCode\":400}}";
    ErrorResponse errorResponse = new ErrorResponse();
    StringUtils.processRawMessage(errorResponse, raw);
    String result = errorResponse.getMessage();
    Assertions.assertFalse(result.contains("@{{"));
  }

  @Test
  void givenPath_whenParse_thenReturnOK() {
    Map<String, String> map = new HashMap<>();
    map.put("id", "123");
    Assertions.assertNotNull(StringUtils.readWithJsonPath(map, "$.id"));
  }
}
