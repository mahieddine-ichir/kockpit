package org.kockpit.communication.filesystem;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FileSystemCommunicationAutoConfiguration {

    @Bean
    Publisher fileSystemPublisher(
            @Value("${kockpit.sdk.filesystem.local_directory}") String localDirectory
    ) {
        return new FileSystemPublisher(localDirectory, objectMapper());
    }

    @Bean
    Consumer fileSystemConsumer(
            @Value("${kockpit.sdk.filesystem.local_directory}") String localDirectory
    ) {
        return new FileSystemConsumer(localDirectory, objectMapper());
    }

    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }
}
