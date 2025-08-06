package com.accor.wcp.console.services.core.servicemanager;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.springframework.util.CollectionUtils.isEmpty;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.appmanifest.AppManifestService;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import com.accor.wcp.console.services.core.appmanifest.AppManifestServiceInternal;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@EnableScheduling
public class ServiceManagerImpl
    implements ApplicationListener<ApplicationStartedEvent>, ServiceManager {

  private final List<WCPConsoleServiceActivator> serviceActivators;

  private final AppManifestService appManifestService;

  private final AppManifestServiceInternal appManifestServiceInternal;

  private Map<String, WCPConsoleServiceMetadata> serviceMetadataMap;

  private Map<String, WCPConsoleServiceActivator> consoleServiceActivatorMap;

  private Map<String, Collection<String>> authorizedGroupsByDomainAndEnv = new HashMap<>();

  private Instant lastLoadedManifestInstant;

  private Collection<? extends AppManifest> lastLoadedAppManifests;

  public ServiceManagerImpl(
      List<WCPConsoleServiceActivator> serviceActivators,
      AppManifestService appManifestService,
      AppManifestServiceInternal appManifestServiceInternal) {
    this.serviceActivators = serviceActivators;
    this.appManifestService = appManifestService;
    this.appManifestServiceInternal = appManifestServiceInternal;
  }

  @Override
  public void onApplicationEvent(ApplicationStartedEvent applicationStartedEvent) {
    processAllManifests();
  }

  public void processAllManifests() {
    // Previously loaded ? => reload
    boolean reload = nonNull(lastLoadedManifestInstant);

    // Keep current timestamp as a savepoint
    lastLoadedManifestInstant = Instant.now();

    lastLoadedAppManifests = appManifestService.getAppManifests();
    serviceMetadataMap =
        serviceActivators.stream()
            .map(
                wcpConsoleServiceActivator ->
                    this.loadServiceActivator(
                        wcpConsoleServiceActivator, lastLoadedAppManifests, reload))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    consoleServiceActivatorMap =
        serviceActivators.stream()
            .collect(toMap(WCPConsoleServiceActivator::getServiceId, identity()));

    authorizedGroupsByDomainAndEnv =
        lastLoadedAppManifests.stream()
            .map(
                appManifest ->
                    new AbstractMap.SimpleEntry<>(
                        appManifest.getDomain() + "-" + appManifest.getEnv(),
                        getGroups(appManifest)))
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, CollectionUtils::union));
  }

  private Collection<String> getGroups(AppManifest appManifest) {
    Collection<String> groups = appManifest.getGroups();
    if (isNull(groups)) {
      return Collections.emptyList();
    }
    return groups;
  }

  private Map.Entry<String, WCPConsoleServiceMetadata> loadServiceActivator(
      WCPConsoleServiceActivator wcpConsoleServiceActivator,
      Collection<? extends AppManifest> appManifests,
      boolean reload) {
    WCPConsoleServiceMetadata wcpConsoleServiceMetadata;
    if (reload) {
      // stop / start
      wcpConsoleServiceMetadata = wcpConsoleServiceActivator.reload(appManifests);
    } else {
      // start
      wcpConsoleServiceMetadata = wcpConsoleServiceActivator.load(appManifests);
    }
    return new AbstractMap.SimpleEntry<>(
        wcpConsoleServiceActivator.getServiceId(), wcpConsoleServiceMetadata);
  }

  public List<WCPConsoleServiceActivator> getServiceActivators() {
    return serviceActivators;
  }

  public WCPConsoleServiceActivator getConsoleServiceActivator(String serviceId) {
    return consoleServiceActivatorMap.get(serviceId);
  }

  public Map<String, WCPConsoleServiceMetadata> getServiceMetadataMap() {
    return serviceMetadataMap;
  }

  public Collection<String> getAuthorizedGroupsForDomainAndEnv(String domain, String env) {
    String key = domain + "-" + env;
    return authorizedGroupsByDomainAndEnv.get(key);
  }

  @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
  void watchManifestUpdates() {
    if (isNull(lastLoadedManifestInstant)) {
      return;
    }
    // Check updated manifest first
    Collection<? extends AppManifest> updatedAppManifests =
        appManifestServiceInternal.findUpdatedAppManifestsFromModification(
            lastLoadedManifestInstant);
    if (isEmpty(updatedAppManifests)) {
      // Check any removed manifest?
      Collection<AppManifest> allManifests = appManifestService.getAppManifests();
      if (nonNull(lastLoadedAppManifests) && allManifests.size() == lastLoadedAppManifests.size()) {
        // Nothing
        return;
      } else {
        if (nonNull(lastLoadedAppManifests)) {
          log.info(
              "Some manifest(s) have been removed (previous: {} vs new: {})",
              lastLoadedAppManifests.size(),
              allManifests.size());
        }
      }
    } else {
      log.info("{} manifest(s) have been updated", updatedAppManifests.size());
    }
    // Reload
    this.reload(updatedAppManifests);
  }

  private void reload(Collection<? extends AppManifest> updatedAppManifests) {
    updatedAppManifests.forEach(
        appManifest -> log.info("Updated manifest(s) found: {}", appManifest));
    // v1 = stop all / load all
    processAllManifests();
  }
}
