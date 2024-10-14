package com.consoleconnect.kraken.operator.core.annotation;

import java.lang.annotation.*;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface AuditAction {
  String resource() default "";

  String resourceId() default "";

  String action() default "";

  String description() default "";

  String[] ignoreRequestParams() default {};

  boolean ignoreResponse() default false;

  String conditionOn() default "true";
}
