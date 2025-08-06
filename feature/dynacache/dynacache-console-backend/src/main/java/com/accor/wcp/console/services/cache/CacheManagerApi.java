package com.accor.wcp.console.services.cache;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;

import com.accor.wcp.console.sdk.communication.WCPConsole2AppCommunicationService;
import com.accor.wcp.sdk.service.cache.communication.CacheReloadMessageRequestDto;
import com.accor.wcp.sdk.service.cache.communication.CacheResetStatsMessageRequestDto;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${console.services.path-prefix}/cache")
@RequiredArgsConstructor
@Slf4j
public class CacheManagerApi {

  private final CacheManagerServiceActivator cacheManagerServiceActivator;
  private final WCPConsole2AppCommunicationService consoleCommunicationService;
  private final StateManager stateManager;
  private final HistoCommandManager histoCommandManager;

  @SneakyThrows
  @GetMapping("/{applicationId}/{cacheName}/_reload")
  public List<String> reload(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @PathVariable String cacheName) {
    CacheReloadMessageRequestDto cacheInvalidationMessageDto =
        CacheReloadMessageRequestDto.builder().cache(cacheName).build();
    String broadcastId =
        consoleCommunicationService.broadcast(
            cacheManagerServiceActivator.getServiceId(),
            domain,
            env,
            applicationId,
            cacheInvalidationMessageDto);
    histoCommandManager.addCommand(
        domain,
        env,
        applicationId,
        cacheName,
        broadcastId,
        CacheCommand.builder().command(Command.CLEAR_CACHE).build());
    return singletonList(broadcastId);
  }

  @SneakyThrows
  @GetMapping("/{applicationId}/{cacheName}/_state")
  public ResponseEntity<CacheViewDto> getCacheState(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @PathVariable String cacheName) {
    CacheState cacheState = stateManager.getCacheState(domain, env, applicationId, cacheName);
    if (isNull(cacheState)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.of(Optional.of(new CacheViewDto(cacheState)));
  }

  @SneakyThrows
  @GetMapping("/{applicationId}/{cacheName}/_refresh")
  public List<String> refreshCacheState(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @PathVariable String cacheName) {
    CacheResetStatsMessageRequestDto cacheResetStatsMessageRequestDto =
        CacheResetStatsMessageRequestDto.builder().cache(cacheName).build();
    String broadcastId =
        consoleCommunicationService.broadcast(
            cacheManagerServiceActivator.getServiceId(),
            domain,
            env,
            applicationId,
            cacheResetStatsMessageRequestDto);
    histoCommandManager.addCommand(
        domain,
        env,
        applicationId,
        cacheName,
        broadcastId,
        CacheCommand.builder().command(Command.RESET_STAT).build());
    stateManager.clearCacheState(domain, env, applicationId, cacheName);
    return singletonList(broadcastId);
  }

  @SneakyThrows
  @GetMapping("/{applicationId}/{cacheName}/_commands")
  public ResponseEntity<CacheCommandsViewDto> getCommands(
      @PathVariable String domain,
      @PathVariable String env,
      @PathVariable String applicationId,
      @PathVariable String cacheName) {
    Map<String, CacheCommand> commands =
        histoCommandManager.getCommands(domain, env, applicationId, cacheName);
    if (isNull(commands)) {
      return ResponseEntity.noContent().build();
    }
    return ResponseEntity.of(Optional.of(new CacheCommandsViewDto(commands)));
  }
}
