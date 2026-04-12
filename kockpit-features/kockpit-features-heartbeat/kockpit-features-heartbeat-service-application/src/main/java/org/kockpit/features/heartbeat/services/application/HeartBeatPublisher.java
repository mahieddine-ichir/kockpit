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
import java.util.List;


@RequiredArgsConstructor
@Slf4j
public class HeartBeatPublisher {

    private final List<Publisher> publishers;

    private final TaskScheduler taskScheduler;

    private final HeartBeatServiceDefinition serviceDefinition;

    void start(String domain, String env, String appId, String instanceId, Duration triggerPeriod) {
        log.info("Start heartBeat for domain {}, env {}, scheduler {}", domain, env, triggerPeriod);
        publishers.forEach(Publisher::cleanup);
        taskScheduler.schedule(() -> {
            log.trace("heartBeat for domain {}, env {} and appId {}", domain, env, appId);
            publishers.forEach(publisher -> publisher.publish(Message.builder()
                    .id(appId)
                    .service(serviceDefinition.name())
                    .domain(domain)
                    .env(env)
                    .appId(appId)
                    .body(HeartBeatDto.builder().instanceId(instanceId).appId(appId).build())
                    .build()));
        }, new PeriodicTrigger(triggerPeriod));
    }

    @PreDestroy
    void onStop() {
        log.info("HeartBeat stopped");
    }
}
