package org.kockpit.features.dynaconfig.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a field as a dynamic configuration property.
 * The field will be updated automatically when a dyna-config message
 * with the matching key is received.
 *
 * <p>The enclosing class must be annotated with {@link DynaConfigEnabler}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynaConfigAttribute {

    /**
     * The dyna-config property key. Defaults to the field name if not specified.
     */
    String value() default "";
}