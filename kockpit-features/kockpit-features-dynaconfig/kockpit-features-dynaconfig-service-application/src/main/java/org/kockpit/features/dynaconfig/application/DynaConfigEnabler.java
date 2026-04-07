package org.kockpit.features.dynaconfig.application;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a Spring bean as a participant in dynamic configuration.
 * Fields within the annotated class can be declared as dynamic properties
 * using {@link DynaConfigAttribute}.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DynaConfigEnabler {
}