package com.accor.wcp.sample.sampleflow.flow.context;

import com.accor.wcp.audit.annotation.AuditAttribute;
import com.accor.wcp.audit.annotation.Audited;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Audited
public class SimpleContext {
  @AuditAttribute
  private String result;
  private String input;
  @AuditAttribute
  private Integer number;
}
