package com.accor.wcp.console.sdk.communication;

import java.io.Serializable;

/**
 * Communication interface for service to send messages to application instances. - 1 message to all
 * instances (see {@link #broadcast(String, String, String, String, Serializable)}) - 1 message to a
 * specific instance (see {@link #send(String, String, String, String, String, Serializable)})
 */
public interface WCPConsole2AppCommunicationService {
  String broadcast(
      String serviceId, String domain, String env, String applicationId, Serializable message);

  default String send(
      String serviceId,
      String domain,
      String env,
      String applicationId,
      String instanceId,
      Serializable message) {
    return broadcast(serviceId, domain, env, applicationId, message);
  }
}
