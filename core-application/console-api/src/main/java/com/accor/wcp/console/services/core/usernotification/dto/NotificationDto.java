package com.accor.wcp.console.services.core.usernotification.dto;


import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.UserNotification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationDto implements UserNotification {
    String id;
    Date date;
    NotificationLevelType level;
    String serviceId;
    String applicationId;
    String description;
    String domain;
    String env;

}
