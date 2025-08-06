package com.accor.wcp.console.sdk.notification;

import java.io.Serializable;
import java.util.Date;

/** User notification properties. */
public interface UserNotification extends Serializable {

  Date getDate();

  NotificationLevelType getLevel();

  String getServiceId();

  String getApplicationId();

  String getDescription();
}
