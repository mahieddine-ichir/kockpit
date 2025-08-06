package com.accor.wcp.console.services.sqsdlq;

import com.accor.wcp.console.sdk.notification.NotificationLevelType;
import com.accor.wcp.console.sdk.notification.WCPConsoleUserNotificationService;
import com.accor.wcp.console.sdk.notification.impl.DefaultUserNotification;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.spi.ExceptionHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SqsExceptionHandler implements ExceptionHandler {

  private final WCPConsoleUserNotificationService userNotificationService;
  private Map<String, String> notificationsSent = new HashMap<>();

  public SqsExceptionHandler(WCPConsoleUserNotificationService userNotificationService) {
    this.userNotificationService = userNotificationService;
  }

  @Override
  public void handleException(Throwable exception) {
    log.warn(exception.getMessage());
  }

  @Override
  public void handleException(String message, Throwable exception) {

    DefaultUserNotification notification =
        DefaultUserNotification.builder()
            .applicationId("sqsdlq")
            .date(new Date())
            .description(message)
            .level(NotificationLevelType.WARNING)
            .build();

    if (notificationsSent.containsKey(message)) {
      this.userNotificationService.update(notificationsSent.get(message), notification);
    } else {
      log.warn(exception.getMessage());
      log.warn(message);

      String notifId = this.userNotificationService.create("sqsdlq", notification);
      notificationsSent.put(message, notifId);
    }
  }

  @Override
  public void handleException(String message, Exchange exchange, Throwable exception) {
    log.warn(exception.getMessage());
  }
}
