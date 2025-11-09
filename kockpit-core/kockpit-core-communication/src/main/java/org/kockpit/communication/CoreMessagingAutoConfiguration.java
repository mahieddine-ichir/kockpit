package org.kockpit.communication;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CoreMessagingAutoConfiguration {

    @Bean
    MessageCache inMemoryMessageCache() {
        return new InMemoryMessageCache();
    }
}
