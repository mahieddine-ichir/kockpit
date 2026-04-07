package org.kockpit.features.dynaconfig.application;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.lang.reflect.Field;

@Getter
@RequiredArgsConstructor
class DynaConfigTargetMetadata {

    private final String propertyName;
    private final Field field;
    private final Object target;
}