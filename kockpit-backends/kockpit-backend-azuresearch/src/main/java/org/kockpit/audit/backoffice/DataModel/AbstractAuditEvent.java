package org.kockpit.audit.backoffice.DataModel;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AbstractAuditEvent implements AuditEvent {
  private long startTime;
  private long endTime;
}
