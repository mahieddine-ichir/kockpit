package org.kockpit.service.featureflipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.service.featureflipping.api.FeatureFlippingService;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
public class FeatureFlippingScheduler {

    private final FeatureFlippingService featureFlippingService;

    private final FeatureFlippingCache featureFlippingCache;

    private final TaskScheduler taskScheduler;

    void start(String domain, String env, Duration triggerPeriod) {
        log.info("Start feature flipping for domain {} and env {}, scheduler at periodic {}", domain, env, triggerPeriod);
        taskScheduler.schedule(() -> {
            log.trace("synchronize feature-flags, for domain {} and env {}", domain, env);
            featureFlippingService.findAll(domain, env).forEach(featureFlippingCache::add);
        }, new PeriodicTrigger(triggerPeriod));
    }
}
