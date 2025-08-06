package com.accor.wcp.audit;

import java.util.List;

/**
 * Function interface to compute AuditEvent list at the end ("later"). {@link #compute()} method is
 * called before notifying.
 */
@FunctionalInterface
public interface AuditEventsComputeFunction {
  List<AuditEvent> compute();
}
