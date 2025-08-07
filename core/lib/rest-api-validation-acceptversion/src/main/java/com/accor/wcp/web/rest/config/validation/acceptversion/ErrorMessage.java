package com.accor.wcp.web.rest.config.validation.acceptversion;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
class ErrorMessage {
  private String title;
  private int status;
  private String detail;
  private String code;
}
