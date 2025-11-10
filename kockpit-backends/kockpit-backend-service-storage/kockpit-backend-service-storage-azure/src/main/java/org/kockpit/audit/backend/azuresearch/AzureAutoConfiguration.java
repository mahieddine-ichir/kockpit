package org.kockpit.audit.backend.azuresearch;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.kockpit.backend.services.storage.ConfigApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AzureAutoConfiguration {

    @Bean
    ConfigApiService storageAccountFilesRepository(BlobContainerClient blobContainerClient) {
        return new StorageAccountFilesRepository(blobContainerClient);
    }

    @ConditionalOnMissingBean(BlobServiceClient.class)
    @Bean
    BlobServiceClient storageAccountBlobServiceClient(
            @Value("${kockpit.audit.azure.storage.endpoint}") String storageEndpoint,
            @Value("${kockpit.audit.azure.storage.account}") String accountName,
            @Value("${kockpit.audit.azure.storage.key}") String key
    ) {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new StorageSharedKeyCredential(accountName, key))
                .buildClient();
    }

    @ConditionalOnMissingBean(BlobContainerClient.class)
    @Bean
    BlobContainerClient storageAccountBlobClient(BlobServiceClient blobServiceClient,
                                   @Value("${kockpit.audit.azure.storage.container}") String containerName) {
        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
