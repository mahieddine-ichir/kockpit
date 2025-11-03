package org.kockpit.communication.polling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;
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
        log.info("Start feature flipping for domain {} and env {}, scheduler at periodic {}", domain, env, triggerPeriod);
        taskScheduler.schedule(() -> {
            log.trace("synchronize feature-flags, for domain {}, env {} and appId {}", domain, env, appId);
            Message message = consumer.poll(domain, env, appId, serviceDefinition.name());
            if (message != null) {
                messageCache.add(message);
            }
        }, new PeriodicTrigger(triggerPeriod));
    }
}
