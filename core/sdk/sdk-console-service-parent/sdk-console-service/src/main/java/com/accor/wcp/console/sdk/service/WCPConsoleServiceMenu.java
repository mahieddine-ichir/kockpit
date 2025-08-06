package com.accor.wcp.console.sdk.service;

/** Console service menu definition. */
public interface WCPConsoleServiceMenu {
  default String getParentId() {
    return null;
  }

  String getId();

  String getLabel();

  String getRoute();

  String getEnv();

  String getDomain();

  String getSubDomain();

  String getApp();
}
