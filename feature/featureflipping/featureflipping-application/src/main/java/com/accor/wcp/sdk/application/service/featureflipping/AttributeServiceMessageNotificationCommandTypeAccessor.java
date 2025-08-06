package com.accor.wcp.sdk.application.service.featureflipping;

import com.accor.wcp.sdk.application.service.ServiceMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandTypeAccessor;

import static java.util.Objects.isNull;

class AttributeServiceMessageNotificationCommandTypeAccessor
    implements CommandTypeAccessor<ServiceMessageNotification> {
  AttributeServiceMessageNotificationCommandTypeAccessor() {}

  static AttributeServiceMessageNotificationCommandTypeAccessor create() {
    return new AttributeServiceMessageNotificationCommandTypeAccessor();
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
