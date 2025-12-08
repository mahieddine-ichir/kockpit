package org.kockpit.audit.api;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public abstract class AbstractAuditEvent implements AuditEvent {
  private long startTime;
  private long endTime;
}
