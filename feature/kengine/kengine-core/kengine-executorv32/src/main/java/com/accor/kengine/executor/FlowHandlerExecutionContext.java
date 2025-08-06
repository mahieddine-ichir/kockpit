package com.accor.kengine.executor;

import lombok.Builder;
import lombok.Data;
import org.thepavel.icomponent.metadata.MethodMetadata;

/** POJO to expose through context handler input (method and arguments) and output. */
@Data
@Builder
public class FlowHandlerExecutionContext {

  private Object[] arguments;
  private MethodMetadata methodMetadata;
  private Object result;
}
