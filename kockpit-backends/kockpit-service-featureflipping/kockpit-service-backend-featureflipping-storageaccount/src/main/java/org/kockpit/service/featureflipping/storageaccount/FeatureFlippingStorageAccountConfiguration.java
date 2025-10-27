package org.kockpit.service.featureflipping.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
class FeatureFlippingStorageAccountConfiguration {

    @Bean
    FeatureFlippingStorageAccount featureFlippingStorageAccount(BlobContainerClient blobContainerClient) {
        return new FeatureFlippingStorageAccount(blobContainerClient);
    }

    @Bean
    BlobServiceClient blobServiceClient(
            @Value("${kockpit.feature-flipping.azure.storage.endpoint}") String storageEndpoint,
            @Value("${kockpit.feature-flipping.azure.storage.account}") String accountName,
            @Value("${kockpit.feature-flipping.azure.storage.key}") String key
    ) {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new StorageSharedKeyCredential(accountName, key))
                .buildClient();
    }

    @Bean
    BlobContainerClient blobClient(BlobServiceClient blobServiceClient,
                                   @Value("${kockpit.feature-flipping.azure.storage.container}") String containerName) {
        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
