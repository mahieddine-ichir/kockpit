package com.accor.wcp.console.services.sqsdlq.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SqsSendMessagesDto {
  @NotEmpty @Valid List<SqsMessageDto> messages;
}
