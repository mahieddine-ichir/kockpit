package com.accor.wcp.sdk.application.communication.impl;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import com.accor.wcp.sdk.application.communication.impl.model.ServiceMessageFromWCPConsoleDto;
import com.accor.wcp.sdk.application.service.ServiceApplicationIntegration;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * SDK Application side services manager. It is responsible for collecting {@link
 * ServiceApplicationIntegration} and managing their life cycle. It also manages / dispatches
 * incoming message to targeted service.
 */
@Slf4j
@Service
class ServicesManager {
  private final List<ServiceApplicationIntegration> serviceApplicationIntegrations;
  private final ApplicationInstanceData applicationInstanceData;
  private Map<String, ServiceApplicationIntegration> serviceApplicationIntegrationMap;
  private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

  ServicesManager(
      List<ServiceApplicationIntegration> serviceApplicationIntegrations,
      ApplicationInstanceData applicationInstanceData,
      @Autowired(required = false)
          App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService) {
    this.serviceApplicationIntegrations = serviceApplicationIntegrations;
    this.applicationInstanceData = applicationInstanceData;
    this.app2WCPConsoleCommunicationService = app2WCPConsoleCommunicationService;
  }

  @PostConstruct
  void init() {
    serviceApplicationIntegrationMap =
        serviceApplicationIntegrations.stream()
            .collect(toMap(ServiceApplicationIntegration::getServiceId, identity()));
  }

  void notify(ServiceMessageFromWCPConsoleDto notification) {
    // Service ID
    String serviceId = notification.getServiceId();
    if (isNull(serviceId)) {
      log.warn("Notification with null serviceId (bad request): {}", notification);
      return;
    }

    // Check application ID
    if (!applicationInstanceData.getApplicationId().equals(notification.getApplicationId())) {
      log.debug(
          "Application ID doesn't match. Skipping notification. notification={}", notification);
      return;
    }

    // Check domain
    if (!applicationInstanceData.getDomain().equals(notification.getDomain())) {
      log.debug("Domain doesn't match. Skipping notification. notification={}", notification);
      return;
    }

    // Check env
    if (!applicationInstanceData.getEnv().equals(notification.getEnv())) {
      log.debug("Env doesn't match. Skipping notification. notification={}", notification);
      return;
    }

    // Check instance ID (if not null)
    String instanceId = notification.getInstanceId();
    if (nonNull(instanceId)
        && !instanceId.equals(applicationInstanceData.getApplicationInstance())) {
      // This message is not this instance
      log.debug("Instance ID doesn't match. Skipping notification. notification={}", notification);
      return;
    }

    // Get service integration
    ServiceApplicationIntegration serviceApplicationIntegration =
        serviceApplicationIntegrationMap.get(serviceId);
    if (isNull(serviceApplicationIntegration)) {
      log.info(
          "Notification {} without application service integration (see: {}) ",
          notification,
          serviceApplicationIntegrations);
      return;
    }

    // Notify service
    serviceApplicationIntegration.notification(notification);

    // Ack
    app2WCPConsoleCommunicationService.ack(serviceId, notification.getMessageId());
  }

  Map<String, ServiceApplicationIntegration> getServiceApplicationIntegrationMap() {
    return serviceApplicationIntegrationMap;
  }
}
