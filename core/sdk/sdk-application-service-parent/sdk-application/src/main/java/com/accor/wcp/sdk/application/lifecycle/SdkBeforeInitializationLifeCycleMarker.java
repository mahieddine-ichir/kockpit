package com.accor.wcp.sdk.application.lifecycle;

/**
 * Marker interface to force Spring context to initialize a class before initializing SDK services.
 */
public interface SdkBeforeInitializationLifeCycleMarker {
  // No method as it is a marker interface
}
