package com.accor.wcp.console.services.core.communication.wcp2app;

import com.accor.wcp.console.sdk.communication.WCPConsole2AppCommunicationService;
import java.io.Serializable;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * This implementation hides real communication service(s). We use it to migrate from one
 * implementation to another (SDK 2.4 - Kinesis to SDK 2.5 - S3)
 */
@Primary
@RequiredArgsConstructor
@Service
class FrontWCPConsole2AppCommunicationService implements WCPConsole2AppCommunicationService {

  private final ServiceMessageS3Producer serviceMessageS3Producer;

  @Override
  public String broadcast(
      String serviceId, String domain, String env, String applicationId, Serializable message) {
    return serviceMessageS3Producer.broadcast(serviceId, domain, env, applicationId, message);
  }

  @Override
  public String send(
      String serviceId,
      String domain,
      String env,
      String applicationId,
      String instanceId,
      Serializable message) {
    return serviceMessageS3Producer.send(
        serviceId, domain, env, applicationId, instanceId, message);
  }
}
