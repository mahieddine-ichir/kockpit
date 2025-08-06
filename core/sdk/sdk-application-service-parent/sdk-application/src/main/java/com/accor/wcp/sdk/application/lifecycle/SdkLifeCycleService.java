package com.accor.wcp.sdk.application.lifecycle;

/** SDK Service to get life cycle information or to interact with SDK life cycle. */
public interface SdkLifeCycleService {

  /** @return current sdk life cycle state */
  SdkLifeCycleState getState();
}
