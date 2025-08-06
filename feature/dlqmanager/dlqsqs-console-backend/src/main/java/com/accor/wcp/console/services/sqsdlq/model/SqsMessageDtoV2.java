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
public class SqsMessageDtoV2 {
  private String partitionKey;
  private String id;
  private Long sentTimestamp;
  private @NotNull String body;
  private String domain;
  private String environment;
  private String groupId;
  @Builder.Default private List<SqsAttributeDto> attributes = new ArrayList<>();
  private String comment;
  private SqsDocumentStatus status;
  @Builder.Default private List<SqsDocumentRetry> retries = new ArrayList<>();
}
