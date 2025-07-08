package org.kockpit.audit.backend.azuresearch.configuration;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageAccountClientConfiguration {

    @Bean
    BlobServiceClient blobServiceClient(
            @Value("${kockpit.audit.azure.storage.endpoint}") String storageEndpoint,
            @Value("${kockpit.audit.azure.storage.account}") String accountName,
            @Value("${kockpit.audit.azure.storage.key}") String key
    ) {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new StorageSharedKeyCredential(accountName, key))
                .buildClient();
    }

    @Bean
    BlobContainerClient blobClient(BlobServiceClient blobServiceClient,
                          @Value("${kockpit.audit.azure.storage.container}") String containerName) {
        return blobServiceClient
                .getBlobContainerClient(containerName);
    }
}
