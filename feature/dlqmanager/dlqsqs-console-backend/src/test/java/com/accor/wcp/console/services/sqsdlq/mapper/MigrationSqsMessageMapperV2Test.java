package com.accor.wcp.console.services.sqsdlq.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.AttributeDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsAttributeType;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class MigrationSqsMessageMapperV2Test {
  private final MigrationSqsMessageMapperV2 underTest =
      Mappers.getMapper(MigrationSqsMessageMapperV2.class);

  @Test
  void map_nominal() {
    // GIVEN
    SqsDocument oldEntity =
        SqsDocument.builder()
            .domain("mockDomain")
            .environment("mockEnvironment")
            .queueName("mockQueueName")
            .sentTimestamp(123456789L)
            .id("mockId")
            .evictionDateTime(987654321L)
            .body("mockBody")
            .groupId("mockGroupId")
            .comment("mockComment")
            .attributes(
                List.of(
                    AttributeDocument.builder()
                        .name("mockName")
                        .value("mockValue")
                        .type(SqsAttributeType.STRING)
                        .build()))
            .retries(List.of(SqsDocumentRetry.builder().groupId("mockGroupId").build()))
            .status(SqsDocumentStatus.ANALYSIS_ONGOING)
            .build();

    // WHEN
    SqsDocumentV2 newEntity = underTest.map(oldEntity);

    // THEN
    assertThat(newEntity.getPartitionKey())
        .isEqualTo(
            oldEntity.getDomain()
                + "_"
                + oldEntity.getEnvironment()
                + "_"
                + oldEntity.getQueueName());
    assertThat(newEntity.getId()).isEqualTo(oldEntity.getSentTimestamp() + "_" + oldEntity.getId());
    assertThat(newEntity)
        .matches(n -> n.getSentTimestamp().equals(oldEntity.getSentTimestamp()))
        .matches(n -> n.getEvictionDateTime().equals(oldEntity.getEvictionDateTime()))
        .matches(n -> n.getBody().equals(oldEntity.getBody()))
        .matches(n -> n.getGroupId().equals(oldEntity.getGroupId()))
        .matches(n -> n.getComment().equals(oldEntity.getComment()))
        .matches(n -> n.getAttributes().equals(oldEntity.getAttributes()))
        .matches(n -> n.getRetries().equals(oldEntity.getRetries()))
        .matches(n -> n.getStatus().equals(oldEntity.getStatus()));
  }
}
