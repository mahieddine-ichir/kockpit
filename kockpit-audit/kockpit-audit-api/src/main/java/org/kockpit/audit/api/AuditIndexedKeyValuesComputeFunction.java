package org.kockpit.audit.api;

import java.util.List;

/**
 * Function interface to compute indexed key values list at the end ("later"). {@link #compute()}
 * method is called before notifying.
 */
@FunctionalInterface
public interface AuditIndexedKeyValuesComputeFunction {
  List<IndexedKeyValue> compute();
}
