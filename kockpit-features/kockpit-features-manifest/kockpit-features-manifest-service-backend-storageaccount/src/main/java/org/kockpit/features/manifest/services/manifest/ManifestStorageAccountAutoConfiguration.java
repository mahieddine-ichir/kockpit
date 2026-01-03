package org.kockpit.features.manifest.services.manifest;

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
public class ManifestStorageAccountAutoConfiguration {

    @Bean
    BlobServiceClient manifestBlobServiceClient(
            @Value("${kockpit.manifests.azure.storage.endpoint}") String storageEndpoint,
            @Value("${kockpit.manifests.azure.storage.account}") String accountName,
            @Value("${kockpit.manifests.azure.storage.key}") String key
    ) {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new StorageSharedKeyCredential(accountName, key))
                .buildClient();
    }

    @Bean
    BlobContainerClient manifestBlobContainerClient(
            BlobServiceClient blobServiceClient,
            @Value("${kockpit.manifests.azure.storage.container}") String containerName
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
    ManifestStorageAccountRepository manifestStorageAccountRepository(BlobContainerClient blobContainerClient) {
        return new ManifestStorageAccountRepository(blobContainerClient);
    }
}
