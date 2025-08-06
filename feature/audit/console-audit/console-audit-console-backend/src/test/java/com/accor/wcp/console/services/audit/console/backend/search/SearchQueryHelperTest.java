package com.accor.wcp.console.services.audit.console.backend.search;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.util.FileCopyUtils.copyToString;

import com.accor.wcp.console.services.audit.console.backend.search.dto.Operand;
import com.accor.wcp.console.services.audit.console.backend.search.dto.Operation;
import com.accor.wcp.console.services.audit.console.backend.search.dto.OperationQuery;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchQuery;
import com.accor.wcp.console.services.audit.console.backend.search.dto.SearchType;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.opensearch.index.query.QueryBuilder;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

class SearchQueryHelperTest {

  @Test
  void should_construct_empty_query() throws IOException, JSONException {
    // Given
    Collection<SearchQuery> queries = new ArrayList<>();
    Collection<String> appIds = List.of("app1", "app2");

    // When
    QueryBuilder queryBuilder = SearchQueryHelper.constructQuery(queries, appIds);

    // Then
    assertThat(queryBuilder).isNotNull();
    String expectedQueryJson =
        copyToString(
            new InputStreamReader(getClass().getResourceAsStream("/data/es-queries/empty1.json")));
    JSONAssert.assertEquals(expectedQueryJson, queryBuilder.toString(), JSONCompareMode.LENIENT);
  }

  @Test
  void should_construct_complex_query() throws IOException, JSONException {
    // Given
    SearchQuery searchQuery1 =
        SearchQuery.builder()
            .name("name1")
            .type(SearchType.STRING)
            .operation(
                OperationQuery.builder()
                    .operator(Operation.EQ)
                    .operand(Operand.builder().value("equalsVal1").build())
                    .build())
            .build();
    SearchQuery searchQuery2 =
        SearchQuery.builder()
            .name("name2")
            .type(SearchType.INTEGER)
            .operation(
                OperationQuery.builder()
                    .operator(Operation.BETWEEN)
                    .operand(Operand.builder().value("1000").valueTo("2000").build())
                    .build())
            .build();
    Collection<SearchQuery> queries = List.of(searchQuery1, searchQuery2);
    Collection<String> appIds = List.of("app1", "app2");

    // When
    QueryBuilder queryBuilder = SearchQueryHelper.constructQuery(queries, appIds);

    // Then
    assertThat(queryBuilder).isNotNull();
    String expectedQueryJson =
        copyToString(
            new InputStreamReader(
                getClass().getResourceAsStream("/data/es-queries/complex1.json")));
    JSONAssert.assertEquals(expectedQueryJson, queryBuilder.toString(), JSONCompareMode.LENIENT);
  }
}
