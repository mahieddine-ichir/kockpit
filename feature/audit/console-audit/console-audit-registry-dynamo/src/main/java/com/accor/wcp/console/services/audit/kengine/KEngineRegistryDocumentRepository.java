package com.accor.wcp.console.services.audit.kengine;

import com.accor.wcp.console.services.audit.kengine.dynamodb.KEngineRegistryDocument;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;

@Repository
public class KEngineRegistryDocumentRepository {

  private final DynamoDbTable<KEngineRegistryDocument> kEngineRegistryDocumentDynamoDbTable;

  public KEngineRegistryDocumentRepository(
      DynamoDbTable<KEngineRegistryDocument> kEngineRegistryDocumentDynamoDbTable) {
    this.kEngineRegistryDocumentDynamoDbTable = kEngineRegistryDocumentDynamoDbTable;
  }

  public Optional<KEngineRegistryDocument> findById(String id) {
    return Optional.ofNullable(
        kEngineRegistryDocumentDynamoDbTable.getItem(Key.builder().partitionValue(id).build()));
  }

  public void update(KEngineRegistryDocument kEngineRegistryDocument) {
    kEngineRegistryDocumentDynamoDbTable.updateItem(kEngineRegistryDocument);
  }

  public static String generateFullReferentialId(
      String domain, String env, String applicationId, int hashCode) {
    return domain + "-" + env + "-" + applicationId + "-" + hashCode;
  }
}
