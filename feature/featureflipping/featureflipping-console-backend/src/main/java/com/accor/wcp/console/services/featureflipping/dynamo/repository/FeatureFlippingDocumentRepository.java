package com.accor.wcp.console.services.featureflipping.dynamo.repository;

import com.accor.wcp.console.services.featureflipping.dynamo.domain.FeatureFlippingDocument;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class FeatureFlippingDocumentRepository {

  private final DynamoDbTable<FeatureFlippingDocument> table;

  public FeatureFlippingDocumentRepository(DynamoDbTable<FeatureFlippingDocument> featureFlippingDocumentsTable) {
    this.table = featureFlippingDocumentsTable;
  }

  public Optional<FeatureFlippingDocument> findById(String id) {
    return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id).build()));
  }

  public void save(FeatureFlippingDocument featureFlippingDocument) {
    table.putItem(featureFlippingDocument);
  }
}
