package org.kockpit.backend.storage.local;

import org.kockpit.audit.backend.ConfigApiDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class LocalAutoConfiguration {

    @Bean
    ConfigApiDelegate storageAccountFilesRepository(
            @Value("${kockpit.audit.local.storage-path}") String fileRepository
    ) {
        return new LocalFilesRepository(fileRepository);
    }
}
