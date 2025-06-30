package org.kockpit.rules.executor;

import lombok.Builder;
import lombok.Data;
import org.thepavel.icomponent.metadata.MethodMetadata;

@Data
@Builder
public class FlowHandlerExecutionContext {

  private Object[] arguments;
  private MethodMetadata methodMetadata;
  private Object result;
}
