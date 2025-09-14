package com.accor.wcp.web.rest.config;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ErrorMessage {
  private String title;
  private int status;
  private String detail;
  private String code;
  private List<FieldError> fieldErrors;
}

@Data
@Builder
class FieldError {
  private String objectName;
  private String field;
  private String message;
}
