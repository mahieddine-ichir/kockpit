package com.accor.kengine.seamless;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Predicate {
  @AliasFor("code")
  String value() default "";

  @AliasFor("value")
  String code() default "";

  String documentation() default "No Doc";
}
