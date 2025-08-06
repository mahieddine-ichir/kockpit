package com.accor.wcp.sdk.application.lifecycle.event;

import org.springframework.context.ApplicationEvent;

public class SdkInitializingEvent extends ApplicationEvent {
  public SdkInitializingEvent(Object source) {
    super(source);
  }
}
