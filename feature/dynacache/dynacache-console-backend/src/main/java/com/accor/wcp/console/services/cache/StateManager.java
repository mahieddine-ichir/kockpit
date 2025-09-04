package com.accor.wcp.console.services.cache;

import com.accor.wcp.console.sdk.topology.ApplicationInstanceManager;
import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.sdk.topology.ManagedInstanceListener;
import jakarta.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class StateManager implements ManagedInstanceListener {

  private final Map<String, CacheState> cacheStates = new HashMap<>();

  private final ApplicationInstanceManager applicationInstanceManager;

  StateManager(ApplicationInstanceManager applicationInstanceManager) {
    this.applicationInstanceManager = applicationInstanceManager;
  }

  @PostConstruct
  void init() {
    applicationInstanceManager.addListener(this);
  }

  private String getCacheKey(String domain, String env, String uniqueId, String cacheName) {
    return domain + env + uniqueId + cacheName;
  }

  void updateCacheState(
      String env,
      String domain,
      String applicationId,
      String instanceId,
      Map<String, Object> message) {
    String cacheName = message.get("name").toString();

    log.debug("Handling cache information for {} {} {} {}", domain, env, applicationId, cacheName);
    String key = getCacheKey(domain, env, applicationId, cacheName);
    CacheState cacheState = cacheStates.computeIfAbsent(key, s -> new CacheState());
    InstanceCacheState instanceCacheState =
        cacheState
            .getInstanceCacheStates()
            .computeIfAbsent(instanceId, s -> new InstanceCacheState(instanceId));
    instanceCacheState.setCacheMetrics(
        message.entrySet().stream()
            .map(o -> CacheMetric.builder().name(o.getKey()).value(o.getValue()).build())
            .toList());
  }

  CacheState getCacheState(String domain, String env, String applicationId, String cacheName) {
    return cacheStates.get(getCacheKey(domain, env, applicationId, cacheName));
  }

  void clearCacheState(String domain, String env, String applicationId, String cacheName) {
    log.info("Removing cache information for {} {} {} {}", domain, env, applicationId, cacheName);
    String key = getCacheKey(domain, env, applicationId, cacheName);
    CacheState cacheState = cacheStates.computeIfAbsent(key, s -> new CacheState());
    cacheState.getInstanceCacheStates().clear();
  }

  @Override
  public void onNewInstance(ManagedInstanceEvent event) {
    // Nothing to do here
  }

  @Override
  public void onRemoveInstance(ManagedInstanceEvent event) {
    final String applicationInstance = event.getApplicationInstance();
    // Browse states
    cacheStates
        .values()
        .forEach(cacheState -> clearApplicationInstanceCacheState(applicationInstance, cacheState));
  }

  private void clearApplicationInstanceCacheState(
      String applicationInstance, CacheState cacheState) {
    // Remove all instance cache state linked to removed instance
    Map<String, InstanceCacheState> instanceCacheStates = cacheState.getInstanceCacheStates();
    Set<Map.Entry<String, InstanceCacheState>> entries = instanceCacheStates.entrySet();
    new HashSet<>(entries)
        .stream()
            .filter(e -> e.getValue().getInstanceId().equals(applicationInstance))
            .map(Map.Entry::getKey)
            .forEach(instanceCacheStates::remove);
  }
}
