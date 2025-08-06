package com.accor.wcp.sdk.application.lifecycle.event;

import org.springframework.context.ApplicationEvent;

public class SdkStoppedEvent extends ApplicationEvent {
  public SdkStoppedEvent(Object source) {
    super(source);
  }
}
