package com.accor.wcp.console.services.core.application.heartbit;

import static com.accor.wcp.console.sdk.topology.ManagedInstanceStatus.DEAD;
import static com.accor.wcp.console.sdk.topology.ManagedInstanceStatus.GRACEFUL_DOWN;
import static com.accor.wcp.console.sdk.topology.ManagedInstanceStatus.NEW;
import static java.util.Objects.isNull;

import com.accor.wcp.console.sdk.topology.ManagedInstanceEvent;
import com.accor.wcp.console.sdk.topology.ManagedInstanceListener;
import com.accor.wcp.console.services.core.application.heartbit.model.HeartBitDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class HeartBitInMemoryManager {
    private static final int MAX_HEARTBIT_AGE_MS = 20000;
    private final Map<String, Map<String, Map<String, Map<String, HeartBitDto>>>> heartBitMapByDomainEnvApp = new HashMap<>();
    private final List<ManagedInstanceListener> listeners = new ArrayList<>();

    void updateWith(HeartBitDto heartBitDto) {
        if (heartBitDto.getTimestamp() < System.currentTimeMillis() - MAX_HEARTBIT_AGE_MS) {
            log.warn("Error timestamp (too old) heartBitDto: {}. Skip it", heartBitDto);
            return;
        }

        Map<String, HeartBitDto> heartBitMap = heartBitMapByDomainEnvApp
                .computeIfAbsent(heartBitDto.getDomain(), s -> new HashMap<>())
                .computeIfAbsent(heartBitDto.getEnv(), s -> new HashMap<>())
                .computeIfAbsent(heartBitDto.getApplicationId(), s -> new HashMap<>());

        if (heartBitDto.isEnded()) {
            // If ended => remove heartbit info in map
            heartBitMap
                    .remove(heartBitDto.getApplicationInstance());

            // Notify shutdown
            ManagedInstanceEvent event = ManagedInstanceEvent.builder()
                    .applicationInstance(heartBitDto.getApplicationInstance())
                    .applicationVersion(heartBitDto.getApplicationVersion())
                    .applicationId(heartBitDto.getApplicationId())
                    .domain(heartBitDto.getDomain())
                    .env(heartBitDto.getEnv())
                    .status(GRACEFUL_DOWN)
                    .build();
            listeners.forEach(l -> this.onRemoveInstance(l, event));
        } else {
            // Else update info

            // Notify new instance
            if (!heartBitMap.containsKey(heartBitDto.getApplicationInstance())) {
                ManagedInstanceEvent event = ManagedInstanceEvent.builder()
                        .applicationInstance(heartBitDto.getApplicationInstance())
                        .applicationVersion(heartBitDto.getApplicationVersion())
                        .applicationId(heartBitDto.getApplicationId())
                        .domain(heartBitDto.getDomain())
                        .env(heartBitDto.getEnv())
                        .status(NEW)
                        .build();
                listeners.forEach(l -> this.onNewInstance(l, event));
            }

            heartBitMap
                    .put(heartBitDto.getApplicationInstance(), heartBitDto);
        }

        // Clean up all (hard!)
        heartBitMapByDomainEnvApp.forEach((domain, domainMap) -> domainMap.forEach((env, envMap) -> envMap.forEach((appId, heartBitDtoMap) -> cleanUp(domain, env, appId, heartBitDtoMap))));
    }

    private void onRemoveInstance(ManagedInstanceListener listener, ManagedInstanceEvent event) {
        try {
            listener.onRemoveInstance(event);
        } catch (Throwable t) {
            log.error("Error notifying managed instance {} for event: {}. Error: {}. Skipping ...", listener, event, t.getMessage());
        }
    }

    private void onNewInstance(ManagedInstanceListener listener, ManagedInstanceEvent event) {
        try {
            listener.onNewInstance(event);
        } catch (Throwable t) {
            log.error("Error notifying managed instance {} for event: {}. Error: {}. Skipping ...", listener, event, t.getMessage());
        }
    }

    private void cleanUp(String domain, String env, String appId, Map<String, HeartBitDto> heartBitMap) {
        final long tooOldTimestamp = System.currentTimeMillis() - MAX_HEARTBIT_AGE_MS;
        // Clean too old heartbit
        List<String> oldHeartbitKeys = heartBitMap.entrySet().stream()
                .filter(e -> e.getValue().getTimestamp() < tooOldTimestamp)
                .map(Map.Entry::getKey)
                .toList();
        if (!oldHeartbitKeys.isEmpty()) {
            log.info("Removing 'dead' heartbit {}/{}/{} for keys: {}", domain, env, appId, oldHeartbitKeys);
            oldHeartbitKeys.forEach(key -> {
                HeartBitDto heartBitDto = heartBitMap.get(key);
                try {
                    heartBitMap.remove(key);
                } catch (ConcurrentModificationException e) {
                    log.error("Can not remove key: {} in map: {}. Skip and continue", key, heartBitMap);
                }

                if (isNull(heartBitDto)) {
                    log.error("Heartbit for key: {} is null !!! Skip it", key);
                    return;
                }

                // Notify dead
                ManagedInstanceEvent event = ManagedInstanceEvent.builder()
                    .applicationInstance(heartBitDto.getApplicationInstance())
                    .applicationVersion(heartBitDto.getApplicationVersion())
                    .applicationId(heartBitDto.getApplicationId())
                    .domain(heartBitDto.getDomain())
                    .env(heartBitDto.getEnv())
                    .status(DEAD)
                    .build();
                listeners.forEach(l -> this.onRemoveInstance(l, event));
            });
        }
    }

    public Map<String, Map<String, Map<String, Map<String, HeartBitDto>>>> getHeartBitMapByDomainEnvApp() {
        return heartBitMapByDomainEnvApp;
    }

    public void addListener(ManagedInstanceListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(ManagedInstanceListener listener) {
        this.listeners.remove(listener);
    }
}
