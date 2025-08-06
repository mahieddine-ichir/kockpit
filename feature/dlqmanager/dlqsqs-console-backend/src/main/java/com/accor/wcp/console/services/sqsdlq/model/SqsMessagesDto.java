package com.accor.wcp.console.services.sqsdlq.model;

import com.accor.wcp.console.services.sqsdlq.dynamo.domain.SqsDocumentStatus;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SqsMessagesDto {
  Map<SqsDocumentStatus, Long> nbMessagesByStatus;
  List<SqsMessageDto> messages;
}
