package org.kockpit.rules.audit.flow.serializer;

import lombok.Data;
import org.kockpit.audit.rules.data.model.ExecutionLog;

import java.beans.Transient;
import java.util.Date;

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
