package com.accor.wcp.console.sdk.topology;

/** Application instance properties. */
public interface MangedApplicationInstance {
  String getInstanceId();

  String getApplicationVersion();

  long getLastUpdatedTimestamp();
}
