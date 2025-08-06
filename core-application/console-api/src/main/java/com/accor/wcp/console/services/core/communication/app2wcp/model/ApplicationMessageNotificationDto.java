package com.accor.wcp.console.services.core.communication.app2wcp.model;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class ApplicationMessageNotificationDto implements ApplicationMessageNotification {
    String serviceId;
    String requestMessageId;
    String domain;
    String env;
    String applicationId;
    String instanceId;

    Map<String, Object> message;

}
