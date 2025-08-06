package com.accor.wcp.console.services.audit.console.backend.search;

import com.accor.wcp.console.services.audit.console.backend.search.dto.Operand;
import com.accor.wcp.console.services.audit.console.backend.search.dto.Operation;
import com.accor.wcp.console.services.audit.console.backend.search.dto.OperationQuery;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchQuery;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchType;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.NestedQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.springframework.util.CollectionUtils;

@UtilityClass
class SearchQueryHelper {

  static QueryBuilder constructQuery(Collection<SearchQuery> queries, Collection<String> appIds) {
    if (CollectionUtils.isEmpty(queries) && CollectionUtils.isEmpty(appIds)) {
      return QueryBuilders.matchAllQuery();
    } else {
      BoolQueryBuilder rootBoolQueryBuilder = new BoolQueryBuilder();

      if (!CollectionUtils.isEmpty(appIds)) {
        rootBoolQueryBuilder.must(constructQueryForAppId(appIds));
      }

      if (!CollectionUtils.isEmpty(queries)) {
        rootBoolQueryBuilder.must(constructQueryForSearchParam(queries));
      }

      return rootBoolQueryBuilder;
    }
  }

  private BoolQueryBuilder constructQueryForAppId(Collection<String> appIds) {
    BoolQueryBuilder boolQueryBuilderForAppId = new BoolQueryBuilder();
    appIds.forEach(
        appId ->
            boolQueryBuilderForAppId.should(
                QueryBuilders.termQuery(AuditReportHelper.FIELD_APP_ID_V2, appId)));
    return boolQueryBuilderForAppId;
  }

  private BoolQueryBuilder constructQueryForSearchParam(Collection<SearchQuery> searchQueries) {
    if (CollectionUtils.isEmpty(searchQueries)) {
      return null;
    }

    final BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

    searchQueries.stream()
        // Group by name
        .collect(Collectors.groupingBy(SearchQuery::getName))
        .forEach(
            (fieldName, fieldSearchQueries) -> {
              // All searchQueries with same name have same subtype..
              if (fieldSearchQueries.get(0).getSubtype() != null
                  && fieldSearchQueries
                      .get(0)
                      .getSubtype()
                      .equals(AuditReportHelper.SUB_TYPE_BUILTIN_KV)) {
                // MUST == AND between all fieldName
                boolQueryBuilder.must(
                    constructQueryForKeyValueFieldV2(fieldName, fieldSearchQueries));
              } else {
                boolQueryBuilder.must(constructQueryForRootField(fieldName, fieldSearchQueries));
              }
            });

    return boolQueryBuilder;
  }

  private NestedQueryBuilder constructQueryForKeyValueFieldV2(
      String fieldName, List<SearchQuery> fieldSearchQueries) {

    final SearchType type = fieldSearchQueries.get(0).getType();
    final String fieldValueName = AuditReportHelper.getKvFieldValueName(type);
    final BoolQueryBuilder boolQueryBuilderForNested = new BoolQueryBuilder();

    // Add constrain on keyName
    boolQueryBuilderForNested.must(
        constructQuery(
            AuditReportHelper.FIELD_KV_NESTED_KEY,
            OperationQuery.builder()
                .operand(Operand.builder().value(fieldName).build())
                .operator(Operation.EQ)
                .build(),
            SearchType.STRING));

    // Add constrain on keyValue
    final BoolQueryBuilder boolQueryBuilderForValue = new BoolQueryBuilder();
    fieldSearchQueries.forEach(
        searchQuery -> {
          // OR (should) between all fieldValue sauf dans le cas des dates
          if (type.equals(SearchType.DATE)) {
            boolQueryBuilderForValue.must(
                constructQuery(fieldValueName, searchQuery.getOperation(), type));
          } else {
            boolQueryBuilderForValue.should(
                constructQuery(fieldValueName, searchQuery.getOperation(), type));
          }
        });

    boolQueryBuilderForNested.must(boolQueryBuilderForValue);

    // Nested query on keyName and all KeyValues
    return QueryBuilders.nestedQuery(
            AuditReportHelper.FIELD_KV_NESTED_PATH, boolQueryBuilderForNested, ScoreMode.None)
        .ignoreUnmapped(true);
  }

  private BoolQueryBuilder constructQueryForRootField(
      String fieldName, List<SearchQuery> fieldSearchQueries) {
    final BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    final SearchType type = fieldSearchQueries.get(0).getType();

    fieldSearchQueries.forEach(
        searchQuery -> {
          // OR between all fieldValue sauf dans le cas des dates
          if (type.equals(SearchType.DATE)) {
            boolQueryBuilder.must(constructQuery(fieldName, searchQuery.getOperation(), type));
          } else {
            boolQueryBuilder.should(constructQuery(fieldName, searchQuery.getOperation(), type));
          }
        });

    return boolQueryBuilder;
  }

  private BoolQueryBuilder constructQuery(
      String fieldName, OperationQuery operation, SearchType searchType) {

    final BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
    final Operand operand = operation.getOperand();
    final Operation operator = operation.getOperator();

    boolean textSearch =
        searchType.equals(SearchType.STRING)
            || searchType.equals(SearchType.EXTENSION)
            || searchType.equals(SearchType.TEXT);

    operator.computeQuery(boolQueryBuilder, fieldName, operand, textSearch);
    return boolQueryBuilder;
  }
}
