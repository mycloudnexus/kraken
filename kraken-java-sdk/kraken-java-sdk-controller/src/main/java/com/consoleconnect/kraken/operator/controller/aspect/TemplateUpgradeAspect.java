package com.consoleconnect.kraken.operator.controller.aspect;

import static com.consoleconnect.kraken.operator.controller.enums.SystemStateEnum.CAN_UPGRADE_STATES;

import com.consoleconnect.kraken.operator.controller.model.SystemInfo;
import com.consoleconnect.kraken.operator.controller.service.SystemInfoService;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import lombok.AllArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
@AllArgsConstructor
public class TemplateUpgradeAspect {
  private final SystemInfoService systemInfoService;

  @Around(
      " @annotation(com.consoleconnect.kraken.operator.controller.aspect.TemplateUpgradeBlockChecker)")
  public Object validateAspect(ProceedingJoinPoint pjp) throws Throwable {
    SystemInfo systemInfo = systemInfoService.find();
    if (!CAN_UPGRADE_STATES.contains(systemInfo.getStatus())) {
      throw KrakenException.forbidden("System is upgrading, current operation is not allowed");
    }
    return pjp.proceed();
  }
}
