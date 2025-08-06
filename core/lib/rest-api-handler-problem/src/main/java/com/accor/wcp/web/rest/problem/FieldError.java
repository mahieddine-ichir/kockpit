package com.accor.wcp.web.rest.problem;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class FieldError implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String objectName;

  private final String field;

  private final String message;

  FieldError(org.springframework.validation.FieldError f) {
    this.objectName = f.getObjectName().replaceFirst("DTO$", "");
    this.field = f.getField();
    this.message = isNotBlank(f.getDefaultMessage()) ? f.getDefaultMessage() : f.getCode();
  }
}
