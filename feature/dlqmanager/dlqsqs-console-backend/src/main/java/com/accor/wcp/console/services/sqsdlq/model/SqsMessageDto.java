package com.accor.wcp.console.services.sqsdlq.model;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SqsMessageDto {

  String id;
  Long sentTimestamp;
  @NotNull String body;
  String domain;
  String environment;
  String groupId;
  List<SqsAttributeDto> attributes;
  String comment;
  SqsDocumentStatus status;
  List<SqsDocumentRetry> retries = new ArrayList<>();
}
