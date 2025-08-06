package com.accor.wcp.console.sdk.notification.impl;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.UserNotification;
import java.io.Serializable;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Default user notification implemetation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultUserNotification implements Serializable, UserNotification {
  private Date date;
  private NotificationLevelType level;
  private String serviceId;
  private String applicationId;
  private String description;
  private String domain;
  private String env;
}
