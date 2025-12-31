package org.kockpit.features.manifest.services.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@Slf4j
public class StorageAccountAutoConfiguration {

    @Bean
    BlobServiceClient blobServiceClient(
            @Value("${kockpit.sdk.azure.storage.endpoint}") String storageEndpoint,
            @Value("${kockpit.sdk.azure.storage.account}") String accountName,
            @Value("${kockpit.sdk.azure.storage.key}") String key
    ) {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new StorageSharedKeyCredential(accountName, key))
                .buildClient();
    }

    @Bean
    BlobContainerClient blobContainerClient(
            BlobServiceClient blobServiceClient,
            @Value("${kockpit.sdk.azure.storage.container}") String containerName
    ) {
        log.info(
"""
    \n
    - Storage Account manifest repository, container: {}
    - Endpoint: {}
""", containerName, blobServiceClient.getAccountUrl());
        return blobServiceClient.getBlobContainerClient(containerName);
    }

    @Bean
    StorageAccountRepository storageAccountRepository(BlobContainerClient blobContainerClient) {
        return new StorageAccountRepository(blobContainerClient);
    }
}