package com.accor.wcp.sdk.application.lifecycle.event;

import org.springframework.context.ApplicationEvent;

public class SdkInitializationTimeoutEvent extends ApplicationEvent {
  public SdkInitializationTimeoutEvent(Object source) {
    super(source);
  }
}
