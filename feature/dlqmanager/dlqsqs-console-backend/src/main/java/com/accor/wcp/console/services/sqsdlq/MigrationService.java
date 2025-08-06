package com.accor.wcp.console.services.sqsdlq;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.mapper.MigrationSqsMessageMapperV2;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

@Service
@AllArgsConstructor
public class MigrationService {
  private final MigrationSqsMessageMapperV2 migrationMapper =
      Mappers.getMapper(MigrationSqsMessageMapperV2.class);
  private final DynamoDbTable<SqsDocument> sqsDocumentsTable;
  private final DynamoDbTable<SqsDocumentV2> dynamoDbTableForSqsDlqV2;

  public void migrate() {
    oldTableContent().stream()
        .filter(
            e ->
                isNotBlank(e.getDomain())
                    && isNotBlank(e.getEnvironment())
                    && isNotBlank(e.getQueueName()))
        .forEach(this::migrateImpl);
  }

  private void migrateImpl(SqsDocument oldEntity) {
    dynamoDbTableForSqsDlqV2.putItem(migrationMapper.map(oldEntity));
    sqsDocumentsTable.deleteItem(oldEntity);
  }

  private List<SqsDocument> oldTableContent() {
    return sqsDocumentsTable.scan(ScanEnhancedRequest.builder().build()).stream()
        .map(Page::items)
        .flatMap(Collection::stream)
        .collect(Collectors.toList());
  }
}
