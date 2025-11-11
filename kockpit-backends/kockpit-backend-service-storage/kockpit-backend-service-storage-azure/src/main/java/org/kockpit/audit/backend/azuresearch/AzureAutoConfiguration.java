package org.kockpit.audit.backend.azuresearch;

import com.azure.storage.blob.BlobContainerClient;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AzureAutoConfiguration {

    @Bean
    ConfigApiService storageAccountFilesRepository(BlobContainerClient blobContainerClient) {
        return new StorageAccountFilesRepository(blobContainerClient);
    }
}
