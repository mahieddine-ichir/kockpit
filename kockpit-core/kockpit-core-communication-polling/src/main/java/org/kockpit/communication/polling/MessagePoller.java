package org.kockpit.communication.polling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.MessageCache;
import org.kockpit.core.sdk.ServiceDefinition;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;

@RequiredArgsConstructor
@Slf4j
public class MessagePoller {

    private final Consumer consumer;

    private final MessageCache messageCache;

    private final TaskScheduler taskScheduler;

    private final ServiceDefinition serviceDefinition;

    void start(String domain, String env, String appId, Duration triggerPeriod) {
        log.info("Start {} for domain {} and env {}, scheduler at periodic {}", serviceDefinition.name(), domain, env, triggerPeriod);
        taskScheduler.schedule(() -> {
            log.trace("synchronize {}, for domain {}, env {} and audience {}", serviceDefinition.name(), domain, env, serviceDefinition.audience());
            consumer.poll(domain, env, serviceDefinition.audience(), serviceDefinition.name())
                    .forEach(messageCache::add);
        }, new PeriodicTrigger(triggerPeriod));
    }
}
