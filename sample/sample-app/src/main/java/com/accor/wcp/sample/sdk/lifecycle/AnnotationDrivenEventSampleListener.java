package com.accor.wcp.sample.sdk.lifecycle;

import com.accor.wcp.sdk.application.lifecycle.SdkBeforeInitializationLifeCycleMarker;
import com.accor.wcp.sdk.application.lifecycle.event.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@Slf4j
public class AnnotationDrivenEventSampleListener implements SdkBeforeInitializationLifeCycleMarker {

    public AnnotationDrivenEventSampleListener() {
        log.info("AnnotationDrivenEventListener");
    }

    /**
     * {@link EventListener} is not called at start ... use dedicated class + interface
     * See {@link MySdkInitializingEventListener}
     */
    @EventListener
    public void sdkInitializingEvent(SdkInitializingEvent e) {
        log.info("Handling sdk initializing event: {}", e);
    }

    /**
     * {@link EventListener} is not called at start ... use dedicated class + interface
     * See {@link MySdkInitializingEventListener}
     */
    @EventListener
    public void sdkInitializationTimeoutEvent(SdkInitializationTimeoutEvent e) {
        log.info("Handling sdk initialization timeout event: {}", e);
    }

    /**
     * {@link EventListener} is not called at start ... use dedicated class + interface
     * See {@link MySdkInitializingEventListener}
     */
    @EventListener
    public void sdkRunningEvent(SdkRunningEvent e) {
        log.info("Handling sdk running event: {}", e);
    }

    @EventListener
    public void sdkStoppingEvent(SdkStoppingEvent e) {
        log.info("Handling sdk stopping event: {}", e);
    }

    @EventListener
    public void sdkStoppedEvent(SdkStoppedEvent e) {
        log.info("Handling sdk stopped event: {}", e);
    }
}
