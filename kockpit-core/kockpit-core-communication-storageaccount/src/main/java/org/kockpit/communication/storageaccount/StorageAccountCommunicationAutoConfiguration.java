package org.kockpit.communication.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.MessageJson;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@AutoConfiguration
@ConditionalOnProperty(
        value = "kockpit.communication.azure.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class StorageAccountCommunicationAutoConfiguration {

    @Bean
    Publisher storageAccountPublisher(
            BlobContainerClient blobContainerClient
    ) {
        return new StorageAccountPublisher(blobContainerClient, MessageJson.mapper());
    }

    @Bean
    Consumer storageAccountConsumer(
            BlobContainerClient blobContainerClient
    ) {
        return new StorageAccountConsumer(blobContainerClient, MessageJson.mapper());
    }

    @Bean
    @Primary
    BlobServiceClient blobServiceClient(
            @Value("${kockpit.communication.azure.storage.endpoint}") String storageEndpoint,
            @Value("${kockpit.communication.azure.storage.account}") String accountName,
            @Value("${kockpit.communication.azure.storage.key}") String key
    ) {
        return new BlobServiceClientBuilder()
                .endpoint(storageEndpoint)
                .credential(new StorageSharedKeyCredential(accountName, key))
                .buildClient();
    }

    @Bean
    @Primary
    BlobContainerClient blobClient(BlobServiceClient blobServiceClient,
                                   @Value("${kockpit.communication.azure.storage.container}") String containerName) {
        return blobServiceClient.getBlobContainerClient(containerName);
    }
}
