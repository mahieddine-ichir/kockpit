package com.accor.wcp.console.services.dynaconfig.dynamo.repository;

import com.accor.wcp.console.services.dynaconfig.dynamo.domain.DynaConfigHistoryDocument;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class DynaConfigHistoryDocumentRepository {

  private final DynamoDbTable<DynaConfigHistoryDocument> table;

  public DynaConfigHistoryDocumentRepository(DynamoDbTable<DynaConfigHistoryDocument> dynaConfigHistoryDocumentsTable) {
    this.table = dynaConfigHistoryDocumentsTable;
  }

  public Optional<DynaConfigHistoryDocument> findById(String id) {
    return Optional.ofNullable(table.getItem(Key.builder().partitionValue(id).build()));
  }

  public void save(DynaConfigHistoryDocument dynaConfigHistoryDocument) {
    table.putItem(dynaConfigHistoryDocument);
  }
}
