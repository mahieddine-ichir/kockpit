package com.accor.wcp.sdk.application.communication;

import java.io.Serializable;

/** Communication interface for application to send messages to service (platform side). */
public interface App2WCPConsoleCommunicationService {
  /**
   * Acknowledge a receipt message.
   *
   * @param serviceId service ID that it refers
   * @param messageId message ID
   */
  void ack(String serviceId, String messageId);

  /**
   * Send a message (from application instance) to service.
   *
   * @param serviceId service ID to target
   * @param message serializable message to transfer
   */
  void notify(String serviceId, Serializable message);

  /**
   * Send a response message to a previous notification from service (platform side).
   *
   * @param serviceId service ID that refers to
   * @param requestMessageId original message ID (request)
   * @param message serializable response message
   */
  void notifyResponse(String serviceId, String requestMessageId, Serializable message);
}
