package com.consoleconnect.kraken.operator.core.annotation;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface Auditable {
  String resource() default "";

  String resourceId() default "";
}
