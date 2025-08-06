package com.accor.wcp.sdk.application.communication.impl;

import static java.util.Collections.emptyList;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.lifecycle.SdkBeforeInitializationLifeCycleMarker;
import com.accor.wcp.sdk.application.lifecycle.SdkLifeCycleService;
import com.accor.wcp.sdk.application.lifecycle.SdkLifeCycleState;
import com.accor.wcp.sdk.application.lifecycle.event.*;
import com.accor.wcp.sdk.application.service.ServiceApplicationIntegration;
import com.accor.wcp.sdk.application.service.ServiceApplicationState;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SdkLifeCycleServiceImpl implements SdkLifeCycleService {

  private final SdkApplicationProperties sdkApplicationProperties;
  private final ServicesManager servicesManager;

  private final ApplicationEventPublisher applicationEventPublisher;

  private SdkLifeCycleState state = SdkLifeCycleState.INITIALIZING;

  /** Dependencies injection are only here to manage build it before Sdk initialization. */
  public SdkLifeCycleServiceImpl(
      ApplicationEventPublisher applicationEventPublisher,
      SdkApplicationProperties sdkApplicationProperties,
      ServicesManager servicesManager,
      // Dependencies (force to start services before this)
      @Qualifier("wcp2AppNotificationConsumer") Object wcp2AppNotificationConsumer,
      @Qualifier("heartBitSenderTask") Object heartBitSenderTask,
      List<SdkBeforeInitializationLifeCycleMarker> dependencies) {
    this.applicationEventPublisher = applicationEventPublisher;
    this.sdkApplicationProperties = sdkApplicationProperties;
    this.servicesManager = servicesManager;
    log.debug("wcp2AppNotificationConsumer: {}", wcp2AppNotificationConsumer);
    log.debug("heartBitSenderTask: {}", heartBitSenderTask);
    log.debug("dependencies: {}", dependencies);
  }

  @Override
  public SdkLifeCycleState getState() {
    return state;
  }

  @PostConstruct
  void initialize() throws InterruptedException {
    // Start initialize process for each service
    applicationEventPublisher.publishEvent(new SdkInitializingEvent(this));

    // Read service states
    Map<String, ServiceApplicationState> serviceStates = initializeServiceStates();
    log.info("SDK application services initialization states: {}", serviceStates);

    // Check service state
    List<String> pendingInitializationServiceIds = checkServiceStateLoopWithTimeout();

    // Send SDK lifecycle event (function of pending status)
    if (!pendingInitializationServiceIds.isEmpty()) {
      log.error("Error initializing services: {}", pendingInitializationServiceIds);
      this.state = SdkLifeCycleState.RUNNING_WITH_ERRORS;
      applicationEventPublisher.publishEvent(new SdkInitializationTimeoutEvent(this));
    } else {
      this.state = SdkLifeCycleState.RUNNING;
      applicationEventPublisher.publishEvent(new SdkInitializedEvent(this, state));
    }

    // Read service states after initialization
    serviceStates = getServiceStates();
    log.info("SDK application services states: {}", serviceStates);
  }

  private List<String> checkServiceStateLoopWithTimeout() throws InterruptedException {
    Collection<ServiceApplicationIntegration> serviceApplicationIntegrations =
        servicesManager.getServiceApplicationIntegrationMap().values();
    long initializationTimeoutInSeconds =
        sdkApplicationProperties.getInitializationTimeout().toSeconds();
    List<String> pendingInitializationServiceIds = emptyList();
    for (int i = 0; i < initializationTimeoutInSeconds; i++) {
      pendingInitializationServiceIds =
          serviceApplicationIntegrations.stream()
              .filter(s -> ServiceApplicationState.INITIALIZING.equals(s.getState()))
              .map(ServiceApplicationIntegration::getServiceId)
              .toList();
      if (pendingInitializationServiceIds.isEmpty()) {
        log.info("SDK application services are all initialized.");
        break;
      } else {
        log.info(
            "SDK application services pending initialization: {}", pendingInitializationServiceIds);
        // Wait for 1 second to check again
        Thread.sleep(1000);
      }
    }
    return pendingInitializationServiceIds;
  }

  private Map<String, ServiceApplicationState> initializeServiceStates() {
    Collection<ServiceApplicationIntegration> serviceApplicationIntegrations =
        servicesManager.getServiceApplicationIntegrationMap().values();
    return serviceApplicationIntegrations.stream()
        .map(this::initializeService)
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  private Map<String, ServiceApplicationState> getServiceStates() {
    Collection<ServiceApplicationIntegration> serviceApplicationIntegrations =
        servicesManager.getServiceApplicationIntegrationMap().values();
    return serviceApplicationIntegrations.stream()
        .map(s -> Pair.of(s.getServiceId(), s.getState()))
        .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
  }

  private Pair<String, ServiceApplicationState> initializeService(
      ServiceApplicationIntegration serviceApplicationIntegration) {
    String serviceId = serviceApplicationIntegration.getServiceId();
    ServiceApplicationState serviceApplicationState = serviceApplicationIntegration.initialize();
    return Pair.of(serviceId, serviceApplicationState);
  }

  @PreDestroy
  void stop() {
    this.state = SdkLifeCycleState.STOPPING;
    applicationEventPublisher.publishEvent(new SdkStoppingEvent(this));
    Map<String, ServiceApplicationState> serviceStates =
        servicesManager.getServiceApplicationIntegrationMap().values().stream()
            .map(this::stopService)
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));

    log.info("SDK application service states: {}", serviceStates);
    applicationEventPublisher.publishEvent(new SdkStoppedEvent(this));
    this.state = SdkLifeCycleState.STOPPED;
  }

  private Pair<String, ServiceApplicationState> stopService(
      ServiceApplicationIntegration serviceApplicationIntegration) {
    String serviceId = serviceApplicationIntegration.getServiceId();
    ServiceApplicationState serviceApplicationState = serviceApplicationIntegration.stop();
    return Pair.of(serviceId, serviceApplicationState);
  }
}
