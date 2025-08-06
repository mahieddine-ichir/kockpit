package com.accor.wcp.console.sdk.topology;

import java.util.List;

/**
 * Application instance manager. It handles new instance registration / un-register one / remove
 * dead (ttl) one.
 */
public interface ApplicationInstanceManager {
  List<? extends ManagedApplication> list(String domain, String env);

  void addListener(ManagedInstanceListener listener);

  void removeListener(ManagedInstanceListener listener);
}
