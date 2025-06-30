package org.kockpit.rules.audit.flow;

import lombok.Getter;
import lombok.Setter;
import org.kockpit.audit.api.AbstractAuditEvent;
import org.kockpit.rules.audit.flow.serializer.ExecutionEDTDTO;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class FlowExecutionAuditEvent extends AbstractAuditEvent {

  private final ExecutionEDTDTO executionEDTDTO;

  @Setter
  private String executionName;

  @Setter
  private long creationTimestamp;

  @Setter
  private int position;

}
