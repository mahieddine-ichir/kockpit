package com.accor.wcp.console.services.audit.console.backend.search;

import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReport;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditReportPage;
import com.accor.wcp.console.services.audit.console.backend.search.dto.AuditViewDto;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchQuery;
import com.accor.wcp.console.services.audit.kengine.KEngineRegistryRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.SearchHit;
import org.opensearch.search.SearchHits;
import org.opensearch.search.builder.SearchSourceBuilder;
import org.opensearch.search.sort.FieldSortBuilder;
import org.opensearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

import static com.accor.wcp.console.services.audit.console.backend.search.SearchQueryHelper.constructQuery;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

@Service
@Slf4j
@RequiredArgsConstructor
class AuditRequestService {

  @Value("${search.index_name}")
  private String indexName;

  private final ElasticSearchClientWrapper client;

  private final KEngineRegistryRepository kEngineRegistryRepository;

  /**
   * deprecated, do not support pagination. Kept for backward compatibility.
   */
  public List<AuditReport> search(
          String domain,
          String env,
          AuditViewDto auditViewDto,
          Collection<SearchQuery> queries,
          Optional<Integer> size) {
    return this.search(domain, env, auditViewDto, queries, Optional.empty(), size).getItems();
  }

  @SneakyThrows
  public AuditReportPage search(
          String domain,
          String env,
          AuditViewDto auditViewDto,
          Collection<SearchQuery> queries,
          Optional<Integer> from, Optional<Integer> size) {

    QueryBuilder queryBuilder = constructQuery(queries, auditViewDto.getAppIds());

    SearchSourceBuilder searchSourceBuilder =
            new SearchSourceBuilder()
                    .query(queryBuilder)
                    .sort(new FieldSortBuilder(AuditReportHelper.FIELD_START).order(SortOrder.DESC))
                    .trackTotalHits(true)
                    .from(from.orElse(0))
                    .size(size.orElse(5));

    log.debug("search generated query {}", searchSourceBuilder);

    SearchRequest searchRequest =
        new SearchRequest()
            .source(searchSourceBuilder)
            .indices(AuditReportHelper.getAuditAliasName(domain, indexName, env));

    SearchResponse searchResponse = client.search(searchRequest);
    List<AuditReport> items = Arrays.stream(searchResponse.getHits().getHits())
            .map(AuditReportHelper::convertForViewList).toList();

    return AuditReportPage.builder()
            .items(items)
            .totalSize(Optional.ofNullable(searchResponse.getHits())
                    .map(SearchHits::getTotalHits)
                    .map(totalHits -> totalHits.value)
                    .orElse(0L)
            )
            .from(from.orElse(0))
            .size(size.orElse(5))
            .build();
  }

  @SneakyThrows
  public AuditReport one(String domain, String env, String id) {
    SearchRequest searchRequest =
        new SearchRequest()
            .indices(AuditReportHelper.getAuditAliasName(domain, indexName, env))
            .source(
                new SearchSourceBuilder()
                    .query(QueryBuilders.termQuery(AuditReportHelper.FIELD_ID, id)));

    List<SearchHit> searchHits = client.search(searchRequest, RequestOptions.DEFAULT);

    // No results
    if (searchHits.isEmpty()) {
      return null;
    }

    AuditReport auditReport = AuditReportHelper.convertForViewDetail(searchHits.get(0));

    // special treatment for audit kengine extension
    // Should we extract this?
    customTreatExtensionKengine(auditReport);

    return auditReport;
  }

  private AuditReport customTreatExtensionKengine(AuditReport auditReport) {
    auditReport.getAudits().stream()
        .filter(audit -> nonNull(audit.get("type")) && "kengine.flows".equals(audit.get("type")))
        .flatMap(audit -> ((List<Map<String, Object>>) audit.get("events")).stream())
        .forEach(this::addReferentialToKengineAuditEventProperties);
    return auditReport;
  }

  private void addReferentialToKengineAuditEventProperties(Map<String, Object> kengineProperties) {
    Map<String, Object> executionEDTDTO =
        (Map<String, Object>) kengineProperties.get("executionEDTDTO");

    // Last version of audit report
    String fullRegistryReferentialId = (String) executionEDTDTO.get("fullRegistryReferentialId");
    if (nonNull(fullRegistryReferentialId)) {
      try {
        Map<String, Object> fullRegistry =
            kEngineRegistryRepository.getReferential(fullRegistryReferentialId);

        List<Map<String, Object>> ruleSpecifications =
            (List<Map<String, Object>>) fullRegistry.get("ruleSpecifications");
        Map<Object, Map<String, Object>> specifications =
            ruleSpecifications.stream().collect(toMap(r -> r.get("name"), identity()));
        executionEDTDTO.put("referential", specifications);
        return;
      } catch (IOException e) {
        log.error("Error getting referential.", e);
      }
    }

    deprecatedAddReferentialToKengineAuditEventProperties(executionEDTDTO);
  }

  @Deprecated(forRemoval = true, since = "2.6.1")
  private void deprecatedAddReferentialToKengineAuditEventProperties(
      Map<String, Object> executionEDTDTO) {
    String ruleReferentialId = (String) executionEDTDTO.get("ruleReferentialId");
    if (nonNull(ruleReferentialId)) {
      try {
        executionEDTDTO.put(
            "referential", kEngineRegistryRepository.getReferential(ruleReferentialId));
      } catch (IOException e) {
        log.error("Error getting referential.", e);
      }
    }
  }
}
