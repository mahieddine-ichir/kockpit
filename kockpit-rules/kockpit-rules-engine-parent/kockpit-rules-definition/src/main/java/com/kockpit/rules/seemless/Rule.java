package com.kockpit.rules.seemless;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Component
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface Rule {
  @AliasFor("code")
  String value() default "";

  @AliasFor("value")
  String code() default "";

  String documentation() default "No Doc";
}
