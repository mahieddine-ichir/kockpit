package org.kockpit.audit.backend.azuresearch;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.sdk.SdkApplicationProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import(SdkApplicationProperties.class)
public class AzureAutoConfiguration {

    @Bean
    ConfigApiDelegate storageAccountFilesRepository(BlobContainerClient blobContainerClient, SdkApplicationProperties sdkApplicationProperties) {
        return new StorageAccountFilesRepository(
                blobContainerClient, sdkApplicationProperties
        );
    }

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
