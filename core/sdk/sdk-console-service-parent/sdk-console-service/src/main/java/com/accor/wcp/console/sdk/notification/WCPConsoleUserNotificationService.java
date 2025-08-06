package com.accor.wcp.console.sdk.notification;

/**
 * Service definition which offers methods to notify console users for events / alerts /
 * notifications.
 */
public interface WCPConsoleUserNotificationService {

  /**
   * Create/Send a "core" or "global" service notification. Not linked to a domain / env.
   *
   * @param notification data
   * @return unique notification ID
   */
  String create(String serviceId, UserNotification notification);

  /**
   * Create/Send a domain / env / service notification.
   *
   * @param domain linked domain
   * @param env linked environment
   * @param serviceId source service
   * @param notification data
   * @return unique notification ID
   */
  String create(String domain, String env, String serviceId, UserNotification notification);

  /**
   * Cancel a previous created notification.
   *
   * @param notificationId notification ID
   */
  void cancel(String notificationId);

  /**
   * Update existing notification to add new information, or change previous one.
   *
   * @param notificationId notification ID
   * @param notification new notification data
   */
  void update(String notificationId, UserNotification notification);
}
