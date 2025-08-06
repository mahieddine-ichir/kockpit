package com.accor.wcp.audit;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class AbstractAuditEvent implements AuditEvent {
  private long startTime;
  private long endTime;
}
