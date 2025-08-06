package com.accor.wcp.console.sdk.service;

import java.util.Map;

/** Message from application properties. */
public interface ApplicationMessageNotification {

  String getRequestMessageId();

  String getDomain();

  String getEnv();

  String getApplicationId();

  String getInstanceId();

  Map<String, Object> getMessage();
}
