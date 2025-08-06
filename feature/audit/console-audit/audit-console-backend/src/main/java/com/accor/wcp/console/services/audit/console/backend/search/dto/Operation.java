package com.accor.wcp.console.services.audit.console.backend.search.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilder;
import org.opensearch.index.query.QueryBuilders;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Operation {
  GT(
      (boolQueryBuilder, fieldName, operand, textSearch) ->
          boolQueryBuilder.must(QueryBuilders.rangeQuery(fieldName).gt(operand.getValue()))),

  GTE(
      (boolQueryBuilder, fieldName, operand, textSearch) ->
          boolQueryBuilder.must(QueryBuilders.rangeQuery(fieldName).gte(operand.getValue()))),

  LT(
      (boolQueryBuilder, fieldName, operand, textSearch) ->
          boolQueryBuilder.must(QueryBuilders.rangeQuery(fieldName).lt(operand.getValue()))),

  LTE(
      (boolQueryBuilder, fieldName, operand, textSearch) ->
          boolQueryBuilder.must(QueryBuilders.rangeQuery(fieldName).lte(operand.getValue()))),

  EQ(
      QueryBuilderFactory.WILDCARD_TOOLTIP,
      (boolQueryBuilder, fieldName, operand, textSearch) -> {
        QueryBuilder queryBuilder = createQueryBuilder(fieldName, operand.getValue(), textSearch);
        boolQueryBuilder.must(queryBuilder);
      }),

  NOT_EQ(
      QueryBuilderFactory.WILDCARD_TOOLTIP,
      (boolQueryBuilder, fieldName, operand, textSearch) -> {
        QueryBuilder queryBuilder = createQueryBuilder(fieldName, operand.getValue(), textSearch);
        boolQueryBuilder.mustNot(queryBuilder);
      }),

  IN(
      QueryBuilderFactory.IN_TOOLTIP,
      (boolQueryBuilder, fieldName, operand, textSearch) -> {
        String[] values = operand.getValue().split(QueryBuilderFactory.IN_SEPARATOR);
        for (String value : values) {
          QueryBuilder queryBuilder = createQueryBuilder(fieldName, value, textSearch);
          boolQueryBuilder.should(queryBuilder);
        }
      }),

  NOT_IN(
      QueryBuilderFactory.IN_TOOLTIP,
      (boolQueryBuilder, fieldName, operand, textSearch) -> {
        String[] values = operand.getValue().split(QueryBuilderFactory.IN_SEPARATOR);
        for (String value : values) {
          QueryBuilder queryBuilder = createQueryBuilder(fieldName, value, textSearch);
          boolQueryBuilder.mustNot(queryBuilder);
        }
      }),

  BETWEEN(
      (boolQueryBuilder, fieldName, operand, textSearch) ->
          boolQueryBuilder.must(
              QueryBuilders.rangeQuery(fieldName)
                  .from(operand.getValue())
                  .to(operand.getValueTo()))),

  NOT_BETWEEN(
      (boolQueryBuilder, fieldName, operand, textSearch) ->
          boolQueryBuilder.mustNot(
              QueryBuilders.rangeQuery(fieldName)
                  .from(operand.getValue())
                  .to(operand.getValueTo())));

  private final String tooltip;
  private final QueryBuilderFactory queryBuilderFactory;

  Operation(QueryBuilderFactory queryBuilderFactory) {
    this(null, queryBuilderFactory);
  }

  Operation(String tooltip, QueryBuilderFactory queryBuilderFactory) {
    this.tooltip = tooltip;
    this.queryBuilderFactory = queryBuilderFactory;
  }

  public String getName() {
    return this.name();
  }

  public String getTooltip() {
    return tooltip;
  }

  public void computeQuery(
      BoolQueryBuilder boolQueryBuilder, String fieldName, Operand operand, boolean textSearch) {
    this.queryBuilderFactory.computeQuery(boolQueryBuilder, fieldName, operand, textSearch);
  }

  private static QueryBuilder createQueryBuilder(
      String fieldName, String value, boolean textSearch) {
    return (textSearch && value.contains(QueryBuilderFactory.WILDCARD_CHAR))
        ? QueryBuilders.wildcardQuery(fieldName, value).caseInsensitive(true)
        : QueryBuilders.termQuery(fieldName, value).caseInsensitive(textSearch);
  }

  @FunctionalInterface
  public interface QueryBuilderFactory {

    String IN_SEPARATOR = ";";
    String IN_TOOLTIP =
        "Use "
            + IN_SEPARATOR
            + " as value separator and "
            + QueryBuilderFactory.WILDCARD_CHAR
            + " as wildcard";
    String WILDCARD_CHAR = "*";
    String WILDCARD_TOOLTIP = "Use " + QueryBuilderFactory.WILDCARD_CHAR + " as wildcard";

    void computeQuery(
        BoolQueryBuilder boolQueryBuilder, String fieldName, Operand operand, boolean textSearch);
  }
}
