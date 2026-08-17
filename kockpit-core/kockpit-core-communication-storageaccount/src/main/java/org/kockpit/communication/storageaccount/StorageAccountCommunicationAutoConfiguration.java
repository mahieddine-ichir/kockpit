package org.kockpit.communication.storageaccount;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.kockpit.communication.Consumer;
import org.kockpit.communication.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

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
        return new StorageAccountPublisher(blobContainerClient, objectMapper());
    }

    @Bean
    Consumer storageAccountConsumer(
            BlobContainerClient blobContainerClient
    ) {
        return new StorageAccountConsumer(blobContainerClient, objectMapper());
    }

    ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                        DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                // Jackson 3 trie les proprietes alphabetiquement par defaut ; on conserve l'ordre
                // de declaration (defaut Jackson 2) pour ne pas changer le JSON produit.
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();
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
