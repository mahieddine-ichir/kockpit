package org.kockpit.audit.stream.azure.search;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableScheduling
public class AzureSearchAuditConsumerConfiguration {

    @Bean("azure-search")
    public AuditConsumer auditConsumer(SearchClient searchClient,
                                       SearchIndexClient searchIndexClient) {
        return new AzureSearchIndexer(searchClient, searchIndexClient);
    }

    @Bean
    SearchIndexClient azureSearchIndexer(
            @Value("${kockpit.audit.stream.azure.search.endpoint}") String endpoint,
            @Value("${kockpit.audit.stream.azure.search.api_key}") String apiKey
    ) {
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(apiKey);
        return new SearchIndexClientBuilder()
                .endpoint(endpoint)
                .credential(azureKeyCredential)
                .buildClient();
    }

    @Bean
    SearchClient searchClient(
            @Value("${kockpit.audit.stream.azure.search.endpoint}") String endpoint,
            @Value("${kockpit.audit.stream.azure.search.index_name}") String index,
            @Value("${kockpit.audit.stream.azure.search.api_key}") String apiKey
    ) {
        AzureKeyCredential azureKeyCredential = new AzureKeyCredential(apiKey);
        return new SearchClientBuilder()
                .endpoint(endpoint)
                .credential(azureKeyCredential)
                .indexName(index)
                .buildClient();
    }

}
