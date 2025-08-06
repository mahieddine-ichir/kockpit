package com.accor.wcp.console.services.sqsdlq.mapper;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

// TODO: Remove this mapper once the migration has been done
@Mapper(builder = @Builder(disableBuilder = true))
public interface MigrationSqsMessageMapperV2 {
  SqsDocumentV2 map(SqsDocument source);

  @AfterMapping
  default void afterMapping(@MappingTarget SqsDocumentV2 target, SqsDocument source) {
    target.setPartitionKey(
        String.format(
            "%s_%s_%s", source.getDomain(), source.getEnvironment(), source.getQueueName()));
    target.setId(String.format("%s_%s", source.getSentTimestamp(), source.getId()));
  }
}
