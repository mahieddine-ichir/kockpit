package com.accor.wcp.sdk.application.communication.impl;

import com.accor.wcp.sdk.application.communication.impl.model.ServiceMessageFromWCPConsoleDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.S3Object;

/** Wcp2App notification processor. */
@Component
@Slf4j
class WCP2AppNotificationProcessor {

  private final ObjectMapper objectMapper;
  private final ServicesManager servicesManager;

  WCP2AppNotificationProcessor(ServicesManager servicesManager) {
    this.servicesManager = servicesManager;
    objectMapper = new ObjectMapper();
  }

  void process(S3Object s3Object, byte[] bytes) throws IOException {
    ServiceMessageFromWCPConsoleDto serviceMessageFromWCPConsoleDto =
        objectMapper.readValue(bytes, ServiceMessageFromWCPConsoleDto.class);
    servicesManager.notify(serviceMessageFromWCPConsoleDto);
  }
}
