package org.kockpit.audit.backend.azuresearch;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.kockpit.audit.backend.ConfigApiDelegate;
import org.kockpit.audit.backend.DomainApiDelegate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class AzureAutoConfiguration {

    @Bean
    ConfigApiDelegate storageAccountFilesRepository(BlobContainerClient blobContainerClient) {
        return new StorageAccountFilesRepository(
                blobContainerClient
        );
    }

    @Bean
    DomainApiDelegate azureSearchRepository(SearchClient searchClient) {
        return new AzureSearchRepository(searchClient);
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

    @Bean
    SearchIndexClient azureSearchIndexer(
            @Value("${kockpit.audit.azure.search.endpoint}") String endpoint,
            @Value("${kockpit.audit.azure.search.api_key}") String apiKey
    ) {
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(apiKey);
        return new SearchIndexClientBuilder()
                .endpoint(endpoint)
                .credential(azureKeyCredential)
                .buildClient();
    }

    @Bean
    SearchClient searchClient(
            @Value("${kockpit.audit.azure.search.endpoint}") String endpoint,
            @Value("${kockpit.audit.azure.search.index_name}") String index,
            @Value("${kockpit.audit.azure.search.api_key}") String apiKey
    ) {
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(apiKey);
        return new SearchClientBuilder()
                .endpoint(endpoint)
                .credential(azureKeyCredential)
                .indexName(index)
                .buildClient();
    }
}
