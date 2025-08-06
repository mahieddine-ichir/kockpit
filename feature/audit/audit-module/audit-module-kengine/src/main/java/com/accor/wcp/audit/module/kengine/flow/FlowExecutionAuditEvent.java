package com.accor.wcp.audit.module.kengine.flow;

import com.accor.wcp.audit.AbstractAuditEvent;
import com.accor.wcp.audit.module.kengine.flow.serializer.ExecutionEDTDTO;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class FlowExecutionAuditEvent extends AbstractAuditEvent {

  private final ExecutionEDTDTO executionEDTDTO;

  private String executionName;

  private long creationTimestamp;

  private int position;

  public ExecutionEDTDTO getExecutionEDTDTO() {
    return executionEDTDTO;
  }

  public String getExecutionName() {
    return executionName;
  }

  public long getCreationTimestamp() {
    return creationTimestamp;
  }

  public int getPosition() {
    return position;
  }

  public void setExecutionName(String executionName) {
    this.executionName = executionName;
  }

  public void setCreationTimestamp(long creationTimestamp) {
    this.creationTimestamp = creationTimestamp;
  }

  public void setPosition(int position) {
    this.position = position;
  }
}
