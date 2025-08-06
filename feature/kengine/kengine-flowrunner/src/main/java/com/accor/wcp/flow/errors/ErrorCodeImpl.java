package com.accor.wcp.flow.errors;

import java.util.Map;
import lombok.Data;

@Data
public class ErrorCodeImpl implements ErrorCode {

  private final String title;
  private final int status;
  private final String detail;
  private final Map<String, Object> parameters;
  private final String name;

  public String name() {
    return name;
  }
}
