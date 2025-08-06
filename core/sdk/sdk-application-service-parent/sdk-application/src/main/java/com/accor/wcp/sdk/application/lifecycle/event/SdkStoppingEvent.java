package com.accor.wcp.sdk.application.lifecycle.event;

import org.springframework.context.ApplicationEvent;

public class SdkStoppingEvent extends ApplicationEvent {
  public SdkStoppingEvent(Object source) {
    super(source);
  }
}
