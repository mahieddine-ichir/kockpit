package com.accor.wcp.sdk.application.service.dynaconfig.configproperties;

import java.lang.reflect.Field;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class FoundField {
  private String propertyName;
  private Field field;
  private Object targetObject;
  private Object value;
}
