package com.kockpit.rules.seemless;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ContextParameter {
  String value() default "";

  boolean errorOnNulls() default false;
}
