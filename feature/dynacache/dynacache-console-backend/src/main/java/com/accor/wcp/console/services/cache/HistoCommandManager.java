package com.accor.wcp.console.services.cache;

import com.accor.wcp.sdk.service.cache.communication.CacheInstanceOperationResult;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
class HistoCommandManager {

  private final Map<String, Map<String, CacheCommand>> histoCommands = new HashMap<>();

  public void addCommand(
      String domain,
      String env,
      String applicationId,
      String cacheName,
      String broadcastId,
      CacheCommand command) {
    Map<String, CacheCommand> appHistoCommands =
        histoCommands.computeIfAbsent(
            buildKey(domain, env, applicationId, cacheName), k -> new LinkedHashMap<>());
    appHistoCommands.put(broadcastId, command);
  }

  public void upsertCacheInstanceOperationResult(
      String domain,
      String env,
      String applicationId,
      String cacheName,
      String broadcastId,
      String instanceId,
      CacheInstanceOperationResult cacheInstanceOperationResult) {
    Map<String, CacheCommand> appHistoCommands =
        histoCommands.get(buildKey(domain, env, applicationId, cacheName));
    if (Objects.nonNull(appHistoCommands)) {
      CacheCommand cacheCommand = appHistoCommands.get(broadcastId);
      if (Objects.nonNull(cacheCommand)) {
        cacheCommand.getInstanceStatus().put(instanceId, cacheInstanceOperationResult);
      } else {
        log.warn("Trying to put a result on an unknown command");
      }
    } else {
      log.warn("Trying to put a result on an unknown command");
    }
  }

  public Map<String, CacheCommand> getCommands(
      String domain, String env, String applicationId, String cacheName) {
    return histoCommands.get(buildKey(domain, env, applicationId, cacheName));
  }

  private String buildKey(String domain, String env, String applicationId, String cacheName) {
    return domain + env + applicationId + cacheName;
  }
}
