package org.kockpit.communication.polling;

import org.kockpit.communication.Consumer;
import org.kockpit.communication.MessageCache;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.core.sdk.ServiceDefinition;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Duration;
import java.util.List;

@AutoConfiguration
@EnableScheduling
public class MessagePollingAutoConfiguration {

    @Bean
    InitializingBean messagePoller(
            Consumer consumer, MessageCache messageCache,
            TaskScheduler taskScheduler,
            SdkApplicationProperties applicationProperties,
            @Value("${kockpit.communication.poller.scheduling:PT10M}") String clientScheduling,
            List<ServiceDefinition> serviceDefinitions,
            List<OnMessageListener> onMessageListeners
    ) {
        serviceDefinitions.stream()
                .filter(ServiceDefinition::isPollingEnabled)
                .forEach(serviceDefinition -> {
                    MessagePoller messagePoller = new MessagePoller(consumer, messageCache, taskScheduler, serviceDefinition, onMessageListeners);
                    messagePoller.start(applicationProperties.getDomain(), applicationProperties.getEnv(), applicationProperties.getAppId(), Duration.parse(clientScheduling));
            });

        return () -> {};
    }
}
