package org.kockpit.audit.stream.azure.search;

import com.azure.search.documents.SearchClient;
import com.azure.search.documents.indexes.SearchIndexClient;
import com.azure.search.documents.indexes.models.IndexDocumentsBatch;
import com.azure.search.documents.indexes.models.SearchField;
import com.azure.search.documents.indexes.models.SearchIndex;
import com.azure.search.documents.models.IndexDocumentsResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.audit.stream.api.AuditConsumer;
import org.kockpit.audit.stream.api.AuditReport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class AzureSearchIndexer implements AuditConsumer {

    private final List<AuditReport> auditReports = new ArrayList<>();

    private final SearchClient searchClient;

    private final SearchIndexClient searchIndexClient;

    private final AuditReportMapper auditReportMapper;

    @Value("${kockpit.audit.stream.azure.search.index_name}")
    private String index;

    @PostConstruct
    void initIndex() {
        List<SearchField> searchFields = SearchIndexClient.buildSearchFields(SearchAuditReport.class, null);
        SearchIndex searchIndex = searchIndexClient.getIndex(index);

        List<SearchField> fields = searchFields.stream().filter(searchField ->
                searchIndex.getFields().stream().noneMatch(sf -> sf.getName().equals(searchField.getName()))
        ).toList();
        fields.forEach(searchField -> {
            log.info("field {}, of type {}", searchField.getName(), searchField.getType());
            searchField.getFields().forEach(f -> log.info("(from {}) field {}, of type {}", searchField.getName(), f.getName(), f.getType()));
        });

        searchIndex.getFields().addAll(fields);

        searchIndexClient.createOrUpdateIndex(searchIndex);
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("{}", throwable.getMessage(), throwable);
    }

    @Override
    public void accept(AuditReport auditReport) {
        auditReports.add(auditReport);
    }

    @Scheduled(
            fixedDelayString = "${kockpit.audit.stream.azure.search.scheduler_ms:5000}",
            timeUnit = TimeUnit.MILLISECONDS)
    void index() {
        if (auditReports.isEmpty()) {
            return;
        }
        log.info("Start indexing {} reports", auditReports.size());
        // defensive copy
        AuditReport[] copy = Arrays.copyOf(auditReports.toArray(), auditReports.size(), AuditReport[].class);
        auditReports.clear();

        List<SearchAuditReport> searchReports = Arrays.stream(copy)
                .map(auditReport -> auditReportMapper.map(auditReport))
                .toList();

        IndexDocumentsBatch<SearchAuditReport> batch = new IndexDocumentsBatch<>();
        batch.addUploadActions(searchReports);

        IndexDocumentsResult indexDocumentsResult = searchClient.indexDocuments(batch);
        indexDocumentsResult.getResults().stream().filter(indexingResult ->!indexingResult.isSucceeded())
                .forEach(indexingResult -> log.error("Error indexing document: {}", indexingResult.getErrorMessage()));
    }

}
