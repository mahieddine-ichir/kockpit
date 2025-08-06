package com.accor.wcp.console.services.audit.console.backend.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OperationQuery {

  private Operation operator;
  private Operand operand;
}
