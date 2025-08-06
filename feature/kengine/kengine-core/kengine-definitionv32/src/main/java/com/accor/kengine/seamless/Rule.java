package com.accor.kengine.seamless;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

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
