package com.accor.wcp.console.sdk.service;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import java.util.Collection;

/**
 * Definition interface for: - service definition - service declaration - service activation on
 * WCPConsole core.
 */
public interface WCPConsoleServiceActivator {

  /**
   * Service identifier to reference it everywhere on WCP Console.
   *
   * @return Unique service ID declaration.
   */
  String getServiceId();

  /**
   * Load / Initialize service for given applications.
   *
   * @param appManifests application manifest
   */
  WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests);

  /**
   * Treat notification from application.
   *
   * @param notification notification from application
   */
  default void notification(ApplicationMessageNotification notification) {
    // Nothing to implement by default (if no communication)
  }

  /**
   * Reload service for given applications. By default, this method does nothing and returns null.
   *
   * @param appManifests application manifest
   */
  default WCPConsoleServiceMetadata reload(Collection<? extends AppManifest> appManifests) {
    // Nothing to reload by default
    return null;
  }
}
