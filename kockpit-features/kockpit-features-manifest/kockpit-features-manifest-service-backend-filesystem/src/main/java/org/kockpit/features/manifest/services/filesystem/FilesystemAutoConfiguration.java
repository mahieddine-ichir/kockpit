package org.kockpit.features.manifest.services.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@Slf4j
public class FilesystemAutoConfiguration {

    @Bean
    FilesystemRepository filesystemRepository(
            @Value("${kockpit.sdk.manifest.filesystem.path}") String fileRepositoryPath
    ) {
        log.info(
"""
    \n
    - Filesystem manifest repository, path: {}
""", fileRepositoryPath);
        return new FilesystemRepository(fileRepositoryPath);
    }
}