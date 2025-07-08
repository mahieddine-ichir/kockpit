package org.kockpit.audit.backend.inmemory;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class InMemoryAutoConfiguration {

    @Bean
    DomainApiDelegate domainApiDelegate(ObjectMapper objectMapper) {
        return new InMemorySearch(objectMapper);
    }

    @Bean
    ConfigApiDelegate configApiDelegate(ObjectMapper objectMapper) {
        return new InMemoryConfigRepository(objectMapper);
    }
}
