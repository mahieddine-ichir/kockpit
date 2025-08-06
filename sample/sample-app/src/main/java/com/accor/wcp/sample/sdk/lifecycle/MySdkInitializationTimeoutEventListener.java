package com.accor.wcp.sample.sdk.lifecycle;

import com.accor.wcp.sdk.application.lifecycle.event.SdkInitializationTimeoutEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;

// @Component
@Slf4j
public class MySdkInitializationTimeoutEventListener
    implements ApplicationListener<SdkInitializationTimeoutEvent> {

  @Override
  public void onApplicationEvent(SdkInitializationTimeoutEvent event) {
    log.info("SdkInitializationTimeoutEvent: {}", event);
    throw new RuntimeException("SDK Initialization Timeout. Critical issue. Stopping application.");
  }
}
