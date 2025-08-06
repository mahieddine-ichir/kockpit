package com.accor.wcp.console.services.featureflipping.dynamo.repository;

import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingHistoryDocument;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class FeatureFlippingHistoryDocumentRepository {

  private final DynamoDbTable<FeatureFlippingHistoryDocument> table;

  public FeatureFlippingHistoryDocumentRepository(DynamoDbTable<FeatureFlippingHistoryDocument> dynaConfigHistoryDocumentsTable) {
    this.table = dynaConfigHistoryDocumentsTable;
  }

  public Optional<FeatureFlippingHistoryDocument> findById(String id) {
    return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id).build()));
  }

  public void save(FeatureFlippingHistoryDocument featureFlippingHistoryDocument) {
    table.putItem(featureFlippingHistoryDocument);
  }
}
