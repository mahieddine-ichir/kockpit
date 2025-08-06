package com.accor.wcp.console.sdk.topology;

/** Listener to handle - new instance event (start) - remove instance event (stopped) */
public interface ManagedInstanceListener {
  void onNewInstance(ManagedInstanceEvent event);

  void onRemoveInstance(ManagedInstanceEvent event);
}
