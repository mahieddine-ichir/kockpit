package org.kockpit.rules.seemless;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AuditFlowExecution {
  @AliasFor("audit")
  boolean value() default true;

  @AliasFor("value")
  boolean audit() default true;

  String parameterName() default "";
}
