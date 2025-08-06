package com.accor.wcp.sdk.application.lifecycle.event;

import org.springframework.context.ApplicationEvent;

public class SdkRunningEvent extends ApplicationEvent {
  public SdkRunningEvent(Object source) {
    super(source);
  }
}
