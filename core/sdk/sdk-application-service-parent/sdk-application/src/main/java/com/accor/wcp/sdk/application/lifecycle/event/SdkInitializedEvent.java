package com.accor.wcp.sdk.application.lifecycle.event;

import com.accor.wcp.sdk.application.lifecycle.SdkLifeCycleState;
import org.springframework.context.ApplicationEvent;

public class SdkInitializedEvent extends ApplicationEvent {
  private final SdkLifeCycleState sdkLifeCycleState;

  public SdkInitializedEvent(Object source, SdkLifeCycleState sdkLifeCycleState) {
    super(source);
    this.sdkLifeCycleState = sdkLifeCycleState;
  }

  public SdkLifeCycleState getSdkLifeCycleState() {
    return sdkLifeCycleState;
  }
}
