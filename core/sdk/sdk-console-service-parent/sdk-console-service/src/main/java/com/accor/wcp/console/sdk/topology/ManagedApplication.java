package com.accor.wcp.console.sdk.topology;

import java.util.List;

/** Managed application properties. */
public interface ManagedApplication {
  String getApplicationId();

  String getApplicationName();

  List<? extends MangedApplicationInstance> getInstances();
}
