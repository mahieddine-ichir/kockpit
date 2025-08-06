package com.accor.wcp.sdk.application.service.cache;

import com.accor.wcp.sdk.application.service.ServiceMessageNotification;

@FunctionalInterface
public interface CommandExecutorFunctional {
    void handle(ServiceMessageNotification notification);
}
