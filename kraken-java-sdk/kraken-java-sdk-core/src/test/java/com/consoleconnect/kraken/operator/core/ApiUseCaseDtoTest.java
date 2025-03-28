package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.dto.ApiUseCaseDto;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ApiUseCaseDtoTest {
  @Test
  void test400Mapping() {
    ApiUseCaseDto usecase = new ApiUseCaseDto();
    usecase.setComponentApiKey("mef.sonata.api.order");
    usecase.setMapperKey("mef.sonata.api-target-mapper.order.eline.delete");
    usecase.setWorkflowKey("mef.sonata.api-workflow.order.eline.delete");

    List<String> memberKeys = usecase.membersDeployable(true);
    Assertions.assertEquals(2, memberKeys.size());

    memberKeys = usecase.membersDeployable(false);
    Assertions.assertEquals(1, memberKeys.size());
  }
}
