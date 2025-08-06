package com.accor.kengine.seamless;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE_USE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface ContextResult {
  @AliasFor("propertyName")
  String value() default "";

  @AliasFor("value")
  String propertyName() default "";
}
