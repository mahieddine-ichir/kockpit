package com.accor.wcp.console.services.sqsdlq.mapper;

import static java.util.Objects.isNull;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocument;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDto;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

@Component
@Mapper(componentModel = "spring")
public interface SqsMessageMapper {

  int DEFAULT_TTL_IN_DAYS = 182;

  @Mapping(
      target = "evictionDateTime",
      source = "sqsMessageDto",
      qualifiedByName = "mapEvictionDateTime")
  @Mapping(target = "id", source = "id")
  @Mapping(target = "queueName", source = "queueName")
  SqsDocument toDocument(
      SqsMessageDto sqsMessageDto, String id, String queueName, @Context Integer ttlInDays);

  SqsMessageDto fromDocument(SqsDocument sqsDocument);

  @Named("mapEvictionDateTime")
  default Long mapEvictionDateTime(SqsMessageDto sqsMessageDto, @Context Integer ttlInDays) {
    if (SqsDocumentStatus.RESOLVED.equals(sqsMessageDto.getStatus())) {
      if (isNull(ttlInDays)) {
        ttlInDays = DEFAULT_TTL_IN_DAYS;
      }
      return sqsMessageDto.getSentTimestamp() + ttlInDays * 24L * 3600 * 1000;
    } else {
      return null;
    }
  }
}
