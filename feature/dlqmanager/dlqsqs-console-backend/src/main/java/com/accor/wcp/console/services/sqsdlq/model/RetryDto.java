package com.accor.wcp.console.services.sqsdlq.model;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentRetry;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RetryDto {
  String parentId;
  @NotNull SqsDocumentRetry retry;
}
