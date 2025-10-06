package org.kockpit.backend.services.storage.filesystem;

import org.kockpit.backend.services.storage.ConfigApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class FileSystemAutoConfiguration {

    @Bean
    ConfigApiService storageAccountFilesRepository(
            @Value("${kockpit.audit.local.storage-path}") String fileRepository
    ) {
        return new FileSystemRepository(fileRepository);
    }
}
