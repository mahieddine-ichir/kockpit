package com.accor.wcp.sdk.application.service.dynaconfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface DynaConfigAttribute {

  /**
   * Config property name, default is field name.
   *
   * @return config property nme
   */
  String value() default "";

  /**
   * Config property name, default is field name.
   *
   * @deprecated use {@link #value()} instead
   * @return config property nme
   */
  @Deprecated(since = "2.5.0", forRemoval = true)
  String propertyName() default "";

  /**
   * Use direct access to update property (default behavior) or use setter.
   *
   * @return beanPropertyAccess flag
   */
  boolean beanPropertyAccess() default false;
}
