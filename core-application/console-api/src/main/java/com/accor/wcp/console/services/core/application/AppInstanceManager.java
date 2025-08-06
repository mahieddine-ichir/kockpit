package com.accor.wcp.console.services.core.application;

import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.sdk.topology.ManagedInstanceListener;
import com.accor.wcp.console.services.core.application.heartbit.HeartBitInMemoryManager;
import com.accor.wcp.console.services.core.application.heartbit.model.HeartBitDto;
import com.accor.wcp.console.services.core.application.model.ApplicationDto;
import com.accor.wcp.console.services.core.application.model.ApplicationInstanceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;

@Slf4j
@Service
public class AppInstanceManager implements ApplicationInstanceManager, ManagedInstanceListener {
  private final HeartBitInMemoryManager heartBitInMemoryManager;
  private final List<ManagedInstanceListener> managedInstanceListeners;

  public AppInstanceManager(HeartBitInMemoryManager heartBitInMemoryManager) {
    this.heartBitInMemoryManager = heartBitInMemoryManager;
    this.heartBitInMemoryManager.addListener(this);
    this.managedInstanceListeners = new ArrayList<>();
  }

  public List<ApplicationDto> list(String domain, String env) {
    Map<String, Map<String, Map<String, Map<String, HeartBitDto>>>> heartBitMapByDomainEnvApp = heartBitInMemoryManager.getHeartBitMapByDomainEnvApp();
    if (isNull(heartBitMapByDomainEnvApp)) {
      return emptyList();
    }
    Map<String, Map<String, Map<String, HeartBitDto>>> heartBitMapByEnvApp = heartBitMapByDomainEnvApp.get(domain);
    if(isNull(heartBitMapByEnvApp)) {
      return emptyList();
    }
    Map<String, Map<String, HeartBitDto>> heartBitMapByApp = heartBitMapByEnvApp.get(env);
    if (isNull(heartBitMapByApp)) {
      return emptyList();
    }

    return heartBitMapByApp.entrySet().stream().map(this::convertToApplication).toList();
  }

  @Override
  public void addListener(ManagedInstanceListener listener) {
    managedInstanceListeners.add(listener);
  }

  @Override
  public void removeListener(ManagedInstanceListener listener) {
    managedInstanceListeners.remove(listener);
  }

  private ApplicationDto convertToApplication(Map.Entry<String, Map<String, HeartBitDto>> entry) {
    Map<String, HeartBitDto> instancesHeartBitMap = entry.getValue();
    List<ApplicationInstanceDto> instances =
        instancesHeartBitMap.entrySet().stream().map(this::convertToInstance).toList();
    return ApplicationDto.builder().applicationId(entry.getKey()).instances(instances).build();
  }

  private ApplicationInstanceDto convertToInstance(Map.Entry<String, HeartBitDto> entry) {
    return ApplicationInstanceDto.builder()
        .instanceId(entry.getKey())
        .lastUpdatedTimestamp(entry.getValue().getTimestamp())
        .build();
  }

  @Override
  public void onNewInstance(ManagedInstanceEvent event) {
    // Propagate event to external listeners
    this.managedInstanceListeners.forEach(listener -> {
      try {
        listener.onNewInstance(event);
      } catch (Throwable t) {
        log.error("Error notifying managed instance {} for event: {}. Error: {}. Skipping ...", listener, event, t.getMessage());
      }
    });
  }

  @Override
  public void onRemoveInstance(ManagedInstanceEvent event) {
    // Propagate event to external listeners
    this.managedInstanceListeners.forEach(listener -> {
      try {
        listener.onRemoveInstance(event);
      } catch (Throwable t) {
        log.error("Error notifying managed instance {} for event: {}. Error: {}. Skipping ...", listener, event, t.getMessage());
      }
    });
  }
}
