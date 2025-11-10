package org.kockpit.communication.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
public class StorageAccountCommunicationAutoConfiguration {

    @Bean
    Publisher storageAccountPublisher(
            BlobContainerClient blobContainerClient
    ) {
        return new StorageAccountPublisher(blobContainerClient, objectMapper());
    }

    @Bean
    Consumer storageAccountConsumer(
            BlobContainerClient blobContainerClient
    ) {
        return new StorageAccountConsumer(blobContainerClient, objectMapper());
    }

    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
    }

    @Bean
    @Primary
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
    @Primary
    BlobContainerClient blobClient(BlobServiceClient blobServiceClient,
                                   @Value("${kockpit.sdk.azure.storage.container}") String containerName) {
        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
