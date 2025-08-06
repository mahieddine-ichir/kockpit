package com.accor.wcp.sample.sdk.lifecycle;

import com.accor.wcp.sdk.application.lifecycle.event.SdkInitializingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class MySdkInitializingEventListener implements ApplicationListener<SdkInitializingEvent> {

	@Override
	public void onApplicationEvent(SdkInitializingEvent event) {
		log.info("SdkInitializingEvent: {}", event);
	}
}
