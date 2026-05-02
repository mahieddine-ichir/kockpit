package org.kockpit.backend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.polling.MessagePoller;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.heartbeat.services.HeartBeatDto;
import org.kockpit.features.manifest.services.backend.ManifestBackendService;
import org.kockpit.features.manifest.services.dto.ManifestDto;
import org.kockpit.features.manifest.services.dto.ServiceDto;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.util.*;

@SpringBootApplication(
        scanBasePackages = "org.kockpit"
)
@RequiredArgsConstructor
@Slf4j
public class KockpitBackendApplication implements InitializingBean {

    public static void main(String[] args) {
        SpringApplication.run(KockpitBackendApplication.class, args);
    }

    private final List<Consumer> consumers;
    private final TaskScheduler taskScheduler;
    private final SdkApplicationProperties applicationProperties;
    private final List<OnMessageListener> onMessageListeners;
    private final ManifestBackendService manifestBackendService;

    @Value("${kockpit.backend.heartbeat.poller.scheduling:PT1M}")
    private String clientScheduling;

    @Override
    public void afterPropertiesSet() {
        List<ManifestDto> list = manifestBackendService.list();
        Map<String, Map<String, Set<String>>> domainEnvServices = new HashMap<>();
        for  (ManifestDto manifestDto : list) {
            domainEnvServices.putIfAbsent(manifestDto.getDomain(), new HashMap<>());
            domainEnvServices.get(manifestDto.getDomain())
                    .putIfAbsent(manifestDto.getEnv(), new HashSet<>());

            List<String> services = manifestDto.getServices().stream().map(ServiceDto::getType).toList();
            domainEnvServices.get(manifestDto.getDomain())
                    .get(manifestDto.getEnv())
                    .addAll(services);
        }

        // start health polling for each domain/env declared in the manifests
        domainEnvServices.forEach((domain, value) ->
                value.forEach((env, services) -> {
                    MessagePoller heartBeatMessagePoller = new MessagePoller(consumers, taskScheduler, () -> "heartbeat", onMessageListeners);
                    heartBeatMessagePoller.start(domain, env, null, Duration.parse(clientScheduling));
                }));

        // start polling for each domain/env/service
        domainEnvServices.forEach((domain, value) ->
                value.forEach((env, services) -> services.forEach(service -> {
                    MessagePoller messagePoller = new MessagePoller(consumers, taskScheduler, () -> service, onMessageListeners);
                    messagePoller.start(domain, env, null, Duration.parse(clientScheduling));
                })));
    }

    @EventListener
    public void onApplicationEvent(HeartBeatDto heartBeatDto) {
        onMessageListeners.stream().parallel().forEach(OnMessageListener::republish);
    }

    static class DomainEnv {
        String domain;
        String env;
        public DomainEnv(String domain, String env) {
            this.domain = domain;
            this.env = env;
        }
    }

    static class DomainEnvService extends DomainEnv {
        String service;
        public DomainEnvService(String domain, String env, String service) {
            super(domain, env);
            this.service = service;
        }
    }
}
