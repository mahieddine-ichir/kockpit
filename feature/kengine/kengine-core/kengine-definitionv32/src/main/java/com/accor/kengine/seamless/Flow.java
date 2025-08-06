package com.accor.kengine.seamless;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.thepavel.icomponent.Handler;

@Component
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Handler("flowServiceMethodHandler")
public @interface Flow {
  @AliasFor("id")
  String value() default "";

  @AliasFor("value")
  String id() default "";

  String documentation() default "No doc";

  String[] ruleIds() default {};

  Class[] ruleClasses() default {};
}
