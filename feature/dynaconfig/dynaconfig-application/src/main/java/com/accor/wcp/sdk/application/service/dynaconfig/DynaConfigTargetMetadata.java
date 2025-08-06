package com.accor.wcp.sdk.application.service.dynaconfig;

import lombok.Builder;
import lombok.Data;

import java.lang.reflect.Field;

@Data
@Builder
public class DynaConfigTargetMetadata {
    private String propertyName;
    private Object targetObject;
    private Field field;
    /**
     * Use setter or not (=default).
     */
    private boolean beanPropertyAccess;
    private Object initialValue;
    private Object currentValue;
}
