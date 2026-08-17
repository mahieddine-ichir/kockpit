package org.kockpit.communication.filesystem;

import org.kockpit.communication.Consumer;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
        return new FileSystemPublisher(localDirectory, objectMapper());
    }

    @Bean
    Consumer fileSystemConsumer(
            @Value("${kockpit.communication.filesystem.path}") String localDirectory
    ) {
        return new FileSystemConsumer(localDirectory, objectMapper());
    }

    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
                // de declaration (defaut Jackson 2) pour ne pas changer le JSON produit.
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
    }
}
