package com.accor.wcp.sdk.application.service.dynaconfig;

import static com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition.SERVICE_ID;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import com.accor.wcp.sdk.application.service.ServiceApplicationIntegration;
import com.accor.wcp.sdk.application.service.ServiceApplicationState;
import com.accor.wcp.sdk.application.service.ServiceMessageNotification;
import com.accor.wcp.sdk.command.manager.CommandExecutor;
import com.accor.wcp.sdk.command.manager.CommandManager;
import com.accor.wcp.sdk.service.dynaconfig.communication.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DynaConfigApplicationServiceIntegration implements ServiceApplicationIntegration {

  private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

  private final DynaConfigSpringBeanProcessor dynaConfigSpringBeanProcessor;

  private final DynamicPropertyUpdateHandler dynamicPropertyUpdateHandler;

  private final DynaConfigPropertiesServiceImpl dynaConfigPropertiesService;

  private CommandManager<ServiceMessageNotification> commandManager;

  private ObjectMapper objectMapper;

  private ServiceApplicationState state = ServiceApplicationState.INITIALIZING;

  @PostConstruct
  void init() {
    Map<String, CommandExecutor<ServiceMessageNotification>> commandsMap = new HashMap<>();
    CommandExecutor<ServiceMessageNotification> propertyUpdate = this::propertyUpdate;
    CommandExecutor<ServiceMessageNotification> instanceInitPropertiesUpdateRequest =
        this::instanceInitPropertiesUpdateRequest;
    CommandExecutor<ServiceMessageNotification> propertiesRefresh = this::propertiesRefresh;

    commandsMap.put(PropertyUpdateMessageRequestDto.class.getName(), propertyUpdate);
    commandsMap.put(
        InstanceInitPropertiesUpdateRequestDto.class.getName(),
        instanceInitPropertiesUpdateRequest);
    commandsMap.put(PropertiesRefreshRequestMessageDto.class.getName(), propertiesRefresh);

    commandManager =
        new CommandManager<>(
            AttributeServiceMessageNotificationCommandTypeAccessor.create(), commandsMap);

    objectMapper = new ObjectMapper();
  }

  private void propertiesRefresh(ServiceMessageNotification notification) {
    if (!ServiceApplicationState.INITIALIZING.equals(this.state)) {
      dynaConfigSpringBeanProcessor.sendInitInstancePropertiesMessage();
    }
  }

  private void instanceInitPropertiesUpdateRequest(ServiceMessageNotification notification) {
    InstanceInitPropertiesUpdateRequestDto message =
        getMessage(notification, InstanceInitPropertiesUpdateRequestDto.class);

    // Nothing to update, set running state
    List<PropertyUpdateMessageRequestDto> updates = message.getUpdates();
    if (updates.isEmpty()) {
      state = ServiceApplicationState.RUNNING;
    }

    List<PropertyUpdateMessageResponseDto> updateResults =
        updates.stream()
            .map(
                request -> {
                  String errorMessage = null;
                  DynaConfigOperationResult result;
                  log.debug("Treat property update request {}", request);
                  String propertyName = request.getPropertyName();
                  String newValue = request.getNewValue();
                  try {
                    updatePropertyWithNewValue(propertyName, newValue);
                    result = DynaConfigOperationResult.DONE;
                  } catch (Exception e) {
                    log.warn(
                        "Treat property update request in error {}. Exception: {}",
                        request,
                        e.getMessage());
                    errorMessage = e.getMessage();
                    result = DynaConfigOperationResult.ERROR;
                  }

                  // Save responses to send back to service backend
                  return PropertyUpdateMessageResponseDto.builder()
                      .propertyName(propertyName)
                      .result(result)
                      .errorMessage(errorMessage)
                      .build();
                })
            .toList();
    log.debug("dynaConfigOperationResult: {}", updateResults);

    // Is there any error?
    // Update service state
    ServiceApplicationState previousState = state;
    state =
        updateResults.stream()
            .map(PropertyUpdateMessageResponseDto::getResult)
            .filter(DynaConfigOperationResult.ERROR::equals)
            .findAny()
            .map(r -> ServiceApplicationState.RUNNING_WITH_ERRORS)
            .orElse(ServiceApplicationState.RUNNING);

    // Send response to service
    InstanceInitPropertiesUpdateResponseDto responseDto =
        InstanceInitPropertiesUpdateResponseDto.builder().results(updateResults).build();
    app2WCPConsoleCommunicationService.notifyResponse(
        getServiceId(), notification.getMessageId(), responseDto);

    // Force sending new values to backend to update it
    if (ServiceApplicationState.INITIALIZING.equals(previousState)) {
      // Send new property values
      this.dynaConfigSpringBeanProcessor.sendInitInstancePropertiesMessage();
    }
  }

  private void updatePropertyWithNewValue(String propertyName, String newValue) {
    dynaConfigPropertiesService.updatePropertyWithNewValue(propertyName, newValue);
    dynamicPropertyUpdateHandler.update(propertyName, newValue);
  }

  private void propertyUpdate(ServiceMessageNotification notification) {
    app2WCPConsoleCommunicationService.ack(getServiceId(), notification.getMessageId());
    PropertyUpdateMessageRequestDto message =
        getMessage(notification, PropertyUpdateMessageRequestDto.class);
    String errorMessage = null;

    // Update property
    log.debug("Treat property update message {}", message);
    DynaConfigOperationResult result;
    try {
      updatePropertyWithNewValue(message.getPropertyName(), message.getNewValue());
      result = DynaConfigOperationResult.DONE;
    } catch (Exception e) {
      log.warn("Treat property update message in error {}. Exception: {}", message, e.getMessage());
      errorMessage = e.getMessage();
      result = DynaConfigOperationResult.ERROR;
    }

    // Send response to service
    PropertyUpdateMessageResponseDto responseDto =
        PropertyUpdateMessageResponseDto.builder()
            .result(result)
            .propertyName(message.getPropertyName())
            .errorMessage(errorMessage)
            .build();
    app2WCPConsoleCommunicationService.notifyResponse(
        getServiceId(), notification.getMessageId(), responseDto);
  }

  private <T> T getMessage(ServiceMessageNotification notification, Class<T> type) {
    return objectMapper.convertValue(notification.getMessage(), type);
  }

  @Override
  public String getServiceId() {
    return SERVICE_ID;
  }

  @Override
  public ServiceApplicationState initialize() {
    dynaConfigSpringBeanProcessor.sendInitInstancePropertiesMessage();
    state = ServiceApplicationState.INITIALIZING;
    // Init process already started with @PostConstruct
    return state;
  }

  @Override
  public ServiceApplicationState getState() {
    return state;
  }

  @Override
  public void notification(ServiceMessageNotification notification) {
    log.debug("Notification for dynaconfig: {}", notification);
    commandManager.execute(notification);
  }
}
