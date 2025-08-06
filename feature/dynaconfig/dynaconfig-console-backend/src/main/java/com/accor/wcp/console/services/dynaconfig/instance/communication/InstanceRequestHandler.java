package com.accor.wcp.console.services.dynaconfig.instance.communication;

import static com.accor.wcp.sdk.service.dynaconfig.ServiceDefinition.SERVICE_ID;
import static java.util.Collections.singletonList;

import com.accor.wcp.console.sdk.communication.WCPConsole2AppCommunicationService;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigRequest;
import com.accor.wcp.console.services.dynaconfig.instance.communication.dto.DynaConfigResponse;
import com.accor.wcp.sdk.service.dynaconfig.communication.InstanceInitPropertiesUpdateRequestDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.PropertiesRefreshRequestMessageDto;
import com.accor.wcp.sdk.service.dynaconfig.communication.PropertyUpdateMessageRequestDto;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class InstanceRequestHandler {
  private final WCPConsole2AppCommunicationService communicationService;
  private final Map<String, DynaConfigRequest> requests = new HashMap<>();

  public String broadcastUpdateRequest(
      String domain, String env, String appId, String propKey, String propValue) {
    PropertyUpdateMessageRequestDto request =
        PropertyUpdateMessageRequestDto.builder().propertyName(propKey).newValue(propValue).build();

    String requestId = communicationService.broadcast(SERVICE_ID, domain, env, appId, request);
    requests.put(
        requestId,
        buildDynaConfigRequest(requestId, domain, env, null, appId, singletonList(request)));

    return requestId;
  }

  public String broadcastMultiUpdatesRequest(
      String domain, String env, String appId, Map<String, String> properties) {
    List<PropertyUpdateMessageRequestDto> updates = buildPropertiesUpdatesRequests(properties);
    InstanceInitPropertiesUpdateRequestDto request =
        InstanceInitPropertiesUpdateRequestDto.builder().updates(updates).build();

    String requestId = communicationService.broadcast(SERVICE_ID, domain, env, appId, request);
    requests.put(requestId, buildDynaConfigRequest(requestId, domain, env, null, appId, updates));

    return requestId;
  }

  public String sendUpdateRequestToInstance(
      String domain,
      String env,
      String appId,
      String instanceId,
      String propKey,
      String propValue) {
    PropertyUpdateMessageRequestDto request =
        PropertyUpdateMessageRequestDto.builder().propertyName(propKey).newValue(propValue).build();

    String requestId =
        communicationService.send(SERVICE_ID, domain, env, appId, instanceId, request);
    requests.put(
        requestId,
        buildDynaConfigRequest(requestId, domain, env, instanceId, appId, singletonList(request)));

    return requestId;
  }

  public String sendMultiUpdatesRequestToInstance(
      String domain, String env, String appId, String instanceId, Map<String, String> properties) {
    List<PropertyUpdateMessageRequestDto> updates = buildPropertiesUpdatesRequests(properties);
    InstanceInitPropertiesUpdateRequestDto request =
        InstanceInitPropertiesUpdateRequestDto.builder().updates(updates).build();

    String requestId =
        communicationService.send(SERVICE_ID, domain, env, appId, instanceId, request);
    requests.put(
        requestId, buildDynaConfigRequest(requestId, domain, env, instanceId, appId, updates));

    return requestId;
  }

  public String broadcastRefreshRequest(String domain, String env, String appId) {
    return communicationService.broadcast(
        SERVICE_ID, domain, env, appId, PropertiesRefreshRequestMessageDto.builder().build());
  }

  public String sendRefreshRequestToInstance(
      String domain, String env, String appId, String instanceId) {
    return communicationService.send(
        SERVICE_ID,
        domain,
        env,
        appId,
        instanceId,
        PropertiesRefreshRequestMessageDto.builder().build());
  }

  public List<DynaConfigRequest> getRequests(String domain, String env, String appId) {
    return new ArrayList<>(requests.values())
        .stream()
            .filter(
                r ->
                    domain.equals(r.getDomain())
                        && env.equals(r.getEnv())
                        && appId.equals(r.getAppId()))
            .toList();
  }

  public DynaConfigRequest getRequestById(String requestId) {
    return requests.getOrDefault(requestId, null);
  }

  public DynaConfigRequest linkResponseToRequest(String requestId, DynaConfigResponse response) {
    DynaConfigRequest request = this.getRequestById(requestId);
    if (Objects.nonNull(request)) {
      request.getResponses().add(response);
    }
    return request;
  }

  private List<PropertyUpdateMessageRequestDto> buildPropertiesUpdatesRequests(
      Map<String, String> properties) {
    List<PropertyUpdateMessageRequestDto> updatesRequest = new ArrayList<>();

    for (Entry<String, String> property : properties.entrySet()) {
      String propKey = property.getKey();
      String propValue = property.getValue();
      updatesRequest.add(
          PropertyUpdateMessageRequestDto.builder()
              .propertyName(propKey)
              .newValue(propValue)
              .build());
    }
    return updatesRequest;
  }

  private DynaConfigRequest buildDynaConfigRequest(
      String requestId,
      String domain,
      String env,
      String instanceId,
      String appId,
      List<PropertyUpdateMessageRequestDto> requests) {
    return DynaConfigRequest.builder()
        .requestId(requestId)
        .domain(domain)
        .env(env)
        .appId(appId)
        .messages(requests)
        .instanceId(instanceId)
        .timestamp(System.currentTimeMillis())
        .responses(new ArrayList<>())
        .build();
  }
}
