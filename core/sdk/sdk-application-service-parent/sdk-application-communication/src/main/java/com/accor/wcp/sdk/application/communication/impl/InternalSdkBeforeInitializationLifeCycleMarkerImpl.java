package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.lifecycle.SdkBeforeInitializationLifeCycleMarker;
import org.springframework.stereotype.Component;

@Component
class InternalSdkBeforeInitializationLifeCycleMarkerImpl
    implements SdkBeforeInitializationLifeCycleMarker {
  // Internal marker not to have an empty list
}
