package org.kockpit.communication.filesystem;

import org.kockpit.communication.Consumer;
import org.kockpit.communication.MessageJson;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.communication.filesystem.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class FileSystemCommunicationAutoConfiguration {

    @Bean
    Publisher fileSystemPublisher(
            @Value("${kockpit.communication.filesystem.path}") String localDirectory
    ) {
        return new FileSystemPublisher(localDirectory, MessageJson.mapper());
    }

    @Bean
    Consumer fileSystemConsumer(
            @Value("${kockpit.communication.filesystem.path}") String localDirectory
    ) {
        return new FileSystemConsumer(localDirectory, MessageJson.mapper());
    }

}
