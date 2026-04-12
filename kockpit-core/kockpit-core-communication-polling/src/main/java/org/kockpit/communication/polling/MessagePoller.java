package org.kockpit.communication.polling;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.MessageCache;
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

    private final MessageCache messageCache;

    private final TaskScheduler taskScheduler;

    private final ServiceDefinition serviceDefinition;

    private final List<OnMessageListener> onMessageListeners;

    void start(String domain, String env, String appId, Duration triggerPeriod) {
        log.info("Start {} for domain {} and env {}, scheduler at periodic {}", serviceDefinition.name(), domain, env, triggerPeriod);
        taskScheduler.schedule(() -> consumers.stream()
                .flatMap(consumer -> consumer.poll(domain, env, appId, serviceDefinition.name()).stream())
                .peek(message -> onMessageListeners.forEach(onMessageListener -> {
                    onMessageListener.onMessage(message);

                    // fixme publish an acknowledgement
                    /*
                    publisher.publish(Message.builder()
                                    .appId(message.getAppId())
                                    .creationDate(Instant.now().getEpochSecond())
                                    .domain(message.getDomain())
                                    .env(message.getEnv())
                                    .id(message.getId())
                                    .type(message.getType()+"-ack")
                            .build());
                     */
                }))
                .forEach(messageCache::add), new PeriodicTrigger(triggerPeriod));
    }
}
