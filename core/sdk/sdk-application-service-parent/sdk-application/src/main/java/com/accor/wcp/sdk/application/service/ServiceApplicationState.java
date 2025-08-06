package com.accor.wcp.sdk.application.service;

/** Service application state enumeration. */
public enum ServiceApplicationState {
  INITIALIZING,
  INITIALIZATION_ERROR,
  RUNNING,
  RUNNING_WITH_ERRORS,
  FAILED,
  STOPPING,
  STOPPED
}
