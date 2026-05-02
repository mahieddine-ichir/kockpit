package org.kockpit.communication.polling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Message;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.core.sdk.ServiceDefinition;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class MessagePoller {

    private final List<Consumer> consumers;
    private final TaskScheduler taskScheduler;
    private final ServiceDefinition serviceDefinition;
    private final List<OnMessageListener> onMessageListeners;

    public void start(String domain, String env, String appId, Duration triggerPeriod) {
        log.info("Start {} for domain {} and env {}, scheduler at periodic {}", serviceDefinition.name(), domain, env, triggerPeriod);
        taskScheduler.schedule(() -> consumers.stream()
                .flatMap(consumer -> consumer.poll(domain, env, appId, serviceDefinition.name()).stream())
                .forEach(this::notifyListeners), new PeriodicTrigger(triggerPeriod));
    }

    private void notifyListeners(Message message) {
        onMessageListeners.forEach(listener -> listener.onMessage(message));
    }
}
