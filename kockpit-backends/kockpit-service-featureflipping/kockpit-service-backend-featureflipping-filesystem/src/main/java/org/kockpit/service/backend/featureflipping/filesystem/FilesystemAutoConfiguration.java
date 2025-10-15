package org.kockpit.service.backend.featureflipping.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.service.featureflipping.api.FeatureFlippingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FilesystemAutoConfiguration {

    @Bean
    public FeatureFlippingService filesystemFeatureFlippingRepository(
            @Value("${kockpit.featureflipping.local_directory}") String localDirectory,
            ObjectMapper objectMapper
    ) {
        return new FilesystemFeatureFlippingRepository(localDirectory, objectMapper);
    }
}
