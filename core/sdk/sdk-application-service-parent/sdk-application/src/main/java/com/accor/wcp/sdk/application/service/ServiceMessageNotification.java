package com.accor.wcp.sdk.application.service;

import java.util.Map;

/**
 * Service message structure. It wraps original message with SDK / Communication layer information.
 */
public interface ServiceMessageNotification {
  /**
   * Unique message ID.
   *
   * @return id
   */
  String getMessageId();

  /**
   * Domain message refers to.
   *
   * @return domain
   */
  String getDomain();

  /**
   * Env message refers to.
   *
   * @return env
   */
  String getEnv();

  /** @return targeted application. */
  String getApplicationId();

  /** @return targeted application instance (null = all instances). */
  String getInstanceId();

  /** @return original message serialized as a Map. */
  Map<String, Object> getMessage();
}
