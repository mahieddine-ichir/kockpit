package org.kockpit.audit.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Function;

@Repeatable(AuditAttributes.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
public @interface AuditAttribute {

  /**
   * Audit parameter key, default is field name
   *
   * @return Audit parameter key
   */
  String key() default "";

  /**
   * Path to the field to audit, default is the field itself
   *
   * @return Path to the field to audit
   */
  String path() default "";

  /**
   * A function to apply on field value, default is a noop function.
   *
   * @deprecated must improve design before proposing this kind of feature.
   * @return @see {@link Function}
   */
//  @Deprecated(forRemoval = true, since = "2.3.2")
  Class<? extends Function<Object, Object>> function() default DEFAULT.class;

  @Deprecated
  class DEFAULT implements Function<Object, Object> {

    @Override
    public Object apply(Object s) {
      return s;
    }
  }
}
