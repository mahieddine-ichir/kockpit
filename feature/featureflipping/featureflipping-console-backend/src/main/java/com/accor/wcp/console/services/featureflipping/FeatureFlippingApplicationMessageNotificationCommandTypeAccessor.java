package com.accor.wcp.console.services.featureflipping;

import com.accor.wcp.console.sdk.service.ApplicationMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandTypeAccessor;

import static java.util.Objects.isNull;

class FeatureFlippingApplicationMessageNotificationCommandTypeAccessor
    implements CommandTypeAccessor<ApplicationMessageNotification> {
  FeatureFlippingApplicationMessageNotificationCommandTypeAccessor() {}

  static FeatureFlippingApplicationMessageNotificationCommandTypeAccessor create() {
    return new FeatureFlippingApplicationMessageNotificationCommandTypeAccessor();
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
