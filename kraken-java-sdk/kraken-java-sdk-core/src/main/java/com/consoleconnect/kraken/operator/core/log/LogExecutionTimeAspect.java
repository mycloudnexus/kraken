package com.consoleconnect.kraken.operator.core.log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LogExecutionTimeAspect {
  private static final Logger logger = LoggerFactory.getLogger(LogExecutionTimeAspect.class);

  @Around("@annotation(com.consoleconnect.kraken.operator.core.annotation.LogExecutionTime)")
  public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
    long startTime = System.currentTimeMillis();
    Object result = joinPoint.proceed();
    long endTime = System.currentTimeMillis();
    logger.info("{} cost : {} ms", joinPoint.getSignature(), endTime - startTime);
    return result;
  }
}
