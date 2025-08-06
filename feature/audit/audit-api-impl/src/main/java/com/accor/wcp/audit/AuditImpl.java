package com.accor.wcp.audit;

import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.Data;

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
