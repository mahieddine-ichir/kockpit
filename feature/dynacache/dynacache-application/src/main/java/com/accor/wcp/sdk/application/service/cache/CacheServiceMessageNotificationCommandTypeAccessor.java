package com.accor.wcp.sdk.application.service.cache;

import static java.util.Objects.isNull;

import com.accor.wcp.sdk.application.service.ServiceMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandTypeAccessor;

class CacheServiceMessageNotificationCommandTypeAccessor
    implements CommandTypeAccessor<ServiceMessageNotification> {
  CacheServiceMessageNotificationCommandTypeAccessor() {}

  static CacheServiceMessageNotificationCommandTypeAccessor create() {
    return new CacheServiceMessageNotificationCommandTypeAccessor();
  }

  @Override
  public String getCommandType(ServiceMessageNotification context) {
    Object commandType = context.getMessage().get("__type__");
    if (isNull(commandType)) {
      return null;
    }
    return commandType.toString();
  }
}
