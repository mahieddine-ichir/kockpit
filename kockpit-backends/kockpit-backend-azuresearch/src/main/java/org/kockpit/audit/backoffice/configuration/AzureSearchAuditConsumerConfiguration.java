package org.kockpit.audit.backoffice.configuration;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.search.documents.SearchClient;
import com.azure.search.documents.SearchClientBuilder;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.SearchIndexClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureSearchAuditConsumerConfiguration {

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
