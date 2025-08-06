package com.accor.wcp.console.services.dynaconfig;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandTypeAccessor;

import static java.util.Objects.isNull;

class DynaConfigApplicationMessageNotificationCommandTypeAccessor
    implements CommandTypeAccessor<ApplicationMessageNotification> {
  DynaConfigApplicationMessageNotificationCommandTypeAccessor() {}

  static DynaConfigApplicationMessageNotificationCommandTypeAccessor create() {
    return new DynaConfigApplicationMessageNotificationCommandTypeAccessor();
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
