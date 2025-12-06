package org.kockpit.backend.services.storage.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;


@AutoConfiguration
@Slf4j
public class FileSystemAutoConfiguration {

    @Bean
    ConfigApiService storageAccountFilesRepository(
            @Value("${kockpit.audit.local.storage-path}") String fileRepository
    ) {
        log.info(
"""
    \n
    - File system config, from : {}
""", fileRepository);
        return new FileSystemRepository(fileRepository);
    }
}
