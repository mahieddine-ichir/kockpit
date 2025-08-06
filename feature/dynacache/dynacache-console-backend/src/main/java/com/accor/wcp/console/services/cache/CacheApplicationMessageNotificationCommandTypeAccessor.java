package com.accor.wcp.console.services.cache;

import static java.util.Objects.isNull;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandTypeAccessor;

class CacheApplicationMessageNotificationCommandTypeAccessor
    implements CommandTypeAccessor<ApplicationMessageNotification> {
  CacheApplicationMessageNotificationCommandTypeAccessor() {}

  static CacheApplicationMessageNotificationCommandTypeAccessor create() {
    return new CacheApplicationMessageNotificationCommandTypeAccessor();
  }

  @Override
  public String getCommandType(ApplicationMessageNotification context) {
    Object commandType = context.getMessage().get("__type__");
    if (isNull(commandType)) {
      return null;
    }
    return commandType.toString();
  }
}
