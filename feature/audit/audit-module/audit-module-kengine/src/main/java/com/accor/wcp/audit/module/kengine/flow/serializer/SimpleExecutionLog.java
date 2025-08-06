package com.accor.wcp.audit.module.kengine.flow.serializer;

import com.accor.kengine.audit.model.ExecutionLog;
import java.beans.Transient;
import java.util.Date;
import lombok.Data;

@Data
public class SimpleExecutionLog implements ExecutionLog {

  private String log;

  private String action;

  private Date ts;

  public SimpleExecutionLog(String action, String log, Date ts) {
    this.log = log;
    this.action = action;
    this.ts = ts;
  }

  @Transient
  @Override
  public Date getTimestamp() {
    return ts;
  }

  @Override
  public void setTimestamp(Date timestamp) {
    this.ts = timestamp;
  }
}
