package com.accor.wcp.console.services.sqsdlq.dynamo;

import static com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2.ATTRIBUTE_NAME_PARTITION_KEY;
import static org.apache.commons.lang3.StringUtils.isBlank;

import com.accor.wcp.console.services.sqsdlq.config.ApplicationProperties;
import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import lombok.AllArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchGetItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.BatchWriteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ReadBatch;
import software.amazon.awssdk.enhanced.dynamodb.model.WriteBatch;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.Select;

@AllArgsConstructor
public abstract class DynamoDbRepository<ENTITY_TYPE extends DynamoDbPrimaryKey> {
  public static final int BATCH_LIMIT = 25;
  public static final int FETCH_LIMIT = 10_000;
  public static final OrderBy ORDER_BY = OrderBy.desc;

  private final ApplicationProperties applicationProperties;
  private final DynamoDbClient dynamoDbClientForSqsDlq;
  private final DynamoDbEnhancedClient dynamoDbEnhancedClientForSqsDlq;
  private final DynamoDbTable<ENTITY_TYPE> dynamoDbTable;
  private final Class<ENTITY_TYPE> entityClass;
  private final Integer nbThreadsForDeleteBatch;

  public Optional<ENTITY_TYPE> fetchOne(PartitionKey partitionKey, String sortKey) {
    return Optional.ofNullable(dynamoDbTable.getItem(createKey(partitionKey, sortKey)));
  }

  public List<ENTITY_TYPE> fetchMultiple(PartitionKey partitionKey, Set<String> sortKeys) {
    List<Key> keys = sortKeys.stream().map(sortKey -> createKey(partitionKey, sortKey)).toList();
    List<List<Key>> partitionedKeys = Lists.partition(keys, BATCH_LIMIT);

    BatchGetItemEnhancedRequest.Builder requestBuilder = BatchGetItemEnhancedRequest.builder();
    for (List<Key> partition : partitionedKeys) {
      ReadBatch.Builder<ENTITY_TYPE> readBatchBuilder = ReadBatch.builder(entityClass);
      partition.forEach(readBatchBuilder::addGetItem);
      requestBuilder.addReadBatch(readBatchBuilder.build());
    }
    return dynamoDbEnhancedClientForSqsDlq
        .batchGetItem(requestBuilder.build())
        .resultsForTable(dynamoDbTable)
        .stream()
        .toList();
  }

  private Key createKey(PartitionKey partitionKey, String sortKey) {
    return Key.builder().partitionValue(partitionKey.toString()).sortValue(sortKey).build();
  }

  public List<ENTITY_TYPE> fetchFirstPage(
      PartitionKey partitionKey, Optional<Expression> postFilter) {
    Key startKey = Key.builder().partitionValue(partitionKey.toString()).build();
    QueryConditional queryConditional = QueryConditional.keyEqualTo(startKey);
    return fetchPage(queryConditional, postFilter);
  }

  public List<ENTITY_TYPE> fetchNextPage(
      PartitionKey partitionKey, String lastSortKey, Optional<Expression> postFilter) {
    Key key = Key.builder().partitionValue(partitionKey.toString()).sortValue(lastSortKey).build();
    QueryConditional queryConditional = QueryConditional.sortLessThan(key);
    return fetchPage(queryConditional, postFilter);
  }

  private List<ENTITY_TYPE> fetchPage(
      QueryConditional queryConditional, Optional<Expression> postFilter) {
    QueryEnhancedRequest request =
        QueryEnhancedRequest.builder()
            .queryConditional(queryConditional)
            .filterExpression(postFilter.orElse(null))
            .scanIndexForward(ORDER_BY.getValue())
            .limit(FETCH_LIMIT)
            .build();
    return dynamoDbTable.query(request).stream()
        .flatMap(page -> page.items().stream())
        .limit(FETCH_LIMIT)
        .toList();
  }

  public void update(ENTITY_TYPE entity) {
    dynamoDbTable.updateItem(entity);
  }

  public void put(ENTITY_TYPE entity) {
    dynamoDbTable.putItem(entity);
  }

  public void deleteAll(PartitionKey partitionKey) {
    Optional<String> sortKey = deletePage(getPageForDelete(partitionKey, null));
    while (sortKey.isPresent()) {
      sortKey = deletePage(getPageForDelete(partitionKey, sortKey.get()));
    }
  }

  /**
   * @param keys The elements' key to delete
   * @return The last sortKey
   */
  public Optional<String> deletePage(List<Key> keys) {
    int size = keys.size();
    if (size == 0) {
      return Optional.empty();
    } else if (size == 1) {
      Key key = keys.get(0);
      dynamoDbTable.deleteItem(key);
      return Optional.empty();
    }

    deleteBatch(keys);
    return keys.get(size - 1).sortKeyValue().map(AttributeValue::s);
  }

  public int getCount(PartitionKey partitionKey) {
    QueryRequest request = getQueryRequestBuilder(partitionKey, null);
    QueryResponse queryResponse = fetchCount(request);
    int count = queryResponse.count();
    while (queryResponse.hasLastEvaluatedKey()) {
      queryResponse =
          fetchCount(getQueryRequestBuilder(partitionKey, queryResponse.lastEvaluatedKey()));
      count += queryResponse.count();
    }
    return count;
  }

  private QueryRequest getQueryRequestBuilder(
      PartitionKey partitionKey, Map<String, AttributeValue> exclusiveStartKey) {
    return QueryRequest.builder()
        .tableName(applicationProperties.getTableNameV2())
        .keyConditionExpression(
            ATTRIBUTE_NAME_PARTITION_KEY + " = :" + ATTRIBUTE_NAME_PARTITION_KEY)
        .expressionAttributeValues(
            Map.of(
                ":" + ATTRIBUTE_NAME_PARTITION_KEY,
                AttributeValue.builder().s(partitionKey.toString()).build()))
        .select(Select.COUNT)
        .exclusiveStartKey(exclusiveStartKey)
        .build();
  }

  private QueryResponse fetchCount(QueryRequest request) {
    return dynamoDbClientForSqsDlq.query(request);
  }

  private List<Key> getPageForDelete(PartitionKey partitionKey, String sortKey) {
    List<ENTITY_TYPE> entities =
        isBlank(sortKey)
            ? fetchFirstPage(partitionKey, Optional.empty())
            : fetchNextPage(partitionKey, sortKey, Optional.empty());
    return entities.stream().map(DynamoDbPrimaryKey::getPrimaryKey).toList();
  }

  private void deleteBatch(List<Key> keys) {
    List<List<Key>> partitions = Lists.partition(keys, BATCH_LIMIT);
    ForkJoinPool customThreadPool =
        new ForkJoinPool(Math.min(nbThreadsForDeleteBatch, partitions.size()));
    customThreadPool
        .submit(() -> partitions.parallelStream().forEach(this::deleteBatchImpl))
        .join();
  }

  private void deleteBatchImpl(List<Key> keys) {
    WriteBatch.Builder<ENTITY_TYPE> writeBatchBuilder =
        WriteBatch.builder(entityClass).mappedTableResource(dynamoDbTable);
    keys.forEach(writeBatchBuilder::addDeleteItem);

    BatchWriteItemEnhancedRequest request =
        BatchWriteItemEnhancedRequest.builder().addWriteBatch(writeBatchBuilder.build()).build();
    dynamoDbEnhancedClientForSqsDlq.batchWriteItem(request);
  }
}
