package com.accor.wcp.sample.kengine.warning;

import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.seamless.ContextResult;
import com.accor.wcp.audit.annotation.AuditAttribute;
import com.accor.wcp.audit.annotation.Audited;
import lombok.Data;

@Data
@Audited
@ContextResult
public class FlowResult {
  @AuditAttribute private ExecutionResult flowExecutionResult;
}
