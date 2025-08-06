package com.accor.wcp.console.services.dynaconfig.dynamo.repository;

import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigDocument;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class DynaConfigDocumentRepository {

  private final DynamoDbTable<DynaConfigDocument> table;

  public DynaConfigDocumentRepository(DynamoDbTable<DynaConfigDocument> dynaConfigDocumentsTable) {
    this.table = dynaConfigDocumentsTable;
  }

  public Optional<DynaConfigDocument> findById(String id) {
    return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id).build()));
  }

  public void save(DynaConfigDocument dynaConfigDocument) {
    table.putItem(dynaConfigDocument);
  }
}
