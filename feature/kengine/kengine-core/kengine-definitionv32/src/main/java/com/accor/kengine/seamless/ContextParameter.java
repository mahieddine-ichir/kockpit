package com.accor.kengine.seamless;

import java.lang.annotation.*;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface ContextParameter {
  String value() default "";

  boolean errorOnNulls() default false;
}
