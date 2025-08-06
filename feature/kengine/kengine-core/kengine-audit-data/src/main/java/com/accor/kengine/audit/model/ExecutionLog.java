package com.accor.kengine.audit.model;

import java.util.Date;

public interface ExecutionLog {

  Date getTimestamp();

  void setTimestamp(Date timestamp);

  String getLog();

  void setLog(String log);

  String getAction();

  void setAction(String action);

  @Deprecated
  default Date getTs() {
    return getTimestamp();
  }

  @Deprecated
  default void setTs(Date ts) {
    this.setTimestamp(ts);
  }
}
