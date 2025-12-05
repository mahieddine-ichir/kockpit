package org.kockpit.features.heartbeat.services.application;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.Publisher;
import org.kockpit.features.heartbeat.services.HeartBeatDto;
import org.kockpit.features.heartbeat.services.HeartBeatServiceDefinition;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.PeriodicTrigger;

import java.time.Duration;


@RequiredArgsConstructor
@Slf4j
public class HeartBeatPublisher {

    private final Publisher publisher;

    private final TaskScheduler taskScheduler;

    private final HeartBeatServiceDefinition serviceDefinition;

    void start(String domain, String env, String appId, String instanceId, Duration triggerPeriod) {
        log.info("Start heartBeat for domain {}, env {}, scheduler {}", domain, env, triggerPeriod);
        taskScheduler.schedule(() -> {
            log.trace("heartBeat for domain {}, env {} and appId {}", domain, env, appId);
            publisher.publish(new Message(
                    appId+"-"+instanceId,
                    serviceDefinition.name(),
                    domain,
                    env,
                    appId,
                    HeartBeatDto.builder().instanceId(instanceId).appId(appId).build()
            ));
        }, new PeriodicTrigger(triggerPeriod));
    }

    @PreDestroy
    void onStop() {
        // TODO - remove message (heartbeat)
    }
}
