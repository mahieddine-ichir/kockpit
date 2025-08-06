package com.accor.wcp.console.services.sqsdlq.dynamo;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@Repository
public class SqsDocumentRepository {

  private final DynamoDbTable<SqsDocument> sqsDocumentsTable;

  public SqsDocumentRepository(DynamoDbTable<SqsDocument> sqsDocumentsTable) {
    this.sqsDocumentsTable = sqsDocumentsTable;
  }

  public List<SqsDocument> findAllByExpression(
      String queueName, String domain, String env, List<String> status) {
    Expression expression = buildExpression(queueName, domain, env, status);

    ScanEnhancedRequest scanEnhancedRequest =
        ScanEnhancedRequest.builder().filterExpression(expression).build();

    return sqsDocumentsTable.scan(scanEnhancedRequest).stream()
        .map(Page::items)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }

  private Expression buildExpression(
      String queueName, String domain, String env, List<String> status) {

    Map<String, AttributeValue> expressionAttributeValues =
        new HashMap<>(
            Map.of(
                ":queueValue", AttributeValue.builder().s(queueName).build(),
                ":domainValue", AttributeValue.builder().s(domain).build(),
                ":environmentValue", AttributeValue.builder().s(env).build()));

    String expressionQuery =
        """
                      queueName = :queueValue
                      AND (
                        (#domainName = :domainValue AND environment = :environmentValue)
                        OR
                        (attribute_not_exists(#domainName) AND attribute_not_exists(environment))
                      )
                    """;

    Expression expression;

    if (CollectionUtils.isEmpty(status)) {
      expression =
          Expression.builder()
              .expression(expressionQuery)
              .expressionValues(expressionAttributeValues)
              .expressionNames(Map.of("#domainName", "domain"))
              .build();
    } else {
      expressionAttributeValues.put(":statusValues", AttributeValue.builder().ss(status).build());
      expressionQuery += " AND contains(:statusValues, #statusName)";
      expression =
          Expression.builder()
              .expression(expressionQuery)
              .expressionValues(expressionAttributeValues)
              .expressionNames(Map.of("#domainName", "domain", "#statusName", "status"))
              .build();
    }
    return expression;
  }

  void deleteById(String id) {
    sqsDocumentsTable.deleteItem(Key.builder().partitionValue(id).build());
  }

  public Optional<SqsDocument> findById(String id) {
    return Optional.ofNullable(sqsDocumentsTable.getItem(Key.builder().partitionValue(id).build()));
  }

  public void update(SqsDocument sqsDocument) {
    sqsDocumentsTable.updateItem(sqsDocument);
  }
}
