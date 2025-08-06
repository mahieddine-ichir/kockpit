package com.accor.wcp.console.services.cache;


import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;

@FunctionalInterface
public interface ApplicationMessageNotificationCommandExecutorFunctional {
    void handle(ApplicationMessageNotification notification);
}
