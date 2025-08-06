package com.accor.wcp.flow.errors;

import java.io.Serializable;
import java.util.Map;

public interface ErrorCode extends Serializable {

  String getTitle();

  int getStatus();

  String getDetail();

  Map<String, Object> getParameters();

  /** @return error code */
  String name();
}
