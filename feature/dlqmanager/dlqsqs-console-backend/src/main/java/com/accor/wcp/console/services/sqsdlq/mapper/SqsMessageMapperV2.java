package com.accor.wcp.console.services.sqsdlq.mapper;

import static java.util.Objects.isNull;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentV2;
import com.accor.wcp.console.services.sqsdlq.model.SqsMessageDtoV2;
import java.util.List;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface SqsMessageMapperV2 {

  int DEFAULT_TTL_IN_DAYS = 182;

  @Mapping(target = "evictionDateTime", source = "source", qualifiedByName = "mapEvictionDateTime")
  SqsDocumentV2 toDocument(SqsMessageDtoV2 source, @Context Integer ttlInDays);

  SqsMessageDtoV2 fromDocument(SqsDocumentV2 source);

  List<SqsMessageDtoV2> fromDocument(List<SqsDocumentV2> source);

  @Named("mapEvictionDateTime")
  default Long mapEvictionDateTime(SqsMessageDtoV2 source, @Context Integer ttlInDays) {
    if (SqsDocumentStatus.RESOLVED.equals(source.getStatus())) {
      if (isNull(ttlInDays)) {
        ttlInDays = DEFAULT_TTL_IN_DAYS;
      }
      return source.getSentTimestamp() + ttlInDays * 24L * 3600 * 1000;
    } else {
      return null;
    }
  }
}
