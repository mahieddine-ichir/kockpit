package org.kockpit.audit;

import org.kockpit.audit.api.Audit;
import org.kockpit.audit.api.AuditEvent;
import org.kockpit.audit.api.AuditEventsComputeFunction;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class AuditImpl implements Audit {
  private String type;
  private List<AuditEvent> events;

  private List<AuditEventsComputeFunction> computeFunctions;

  void addEvents(List<AuditEvent> events) {
    this.events.addAll(events);
  }

  public List<AuditEvent> getEvents() {
    return new ArrayList<>(events);
  }

  public void addComputeFunction(AuditEventsComputeFunction computeFunction) {
    computeFunctions.add(computeFunction);
  }

  void close() {
    computeFunctions.forEach(f -> events.addAll(f.compute()));
    computeFunctions = null;
  }
}
