package com.accor.wcp.sample.sampleflow.flow.rule.action;

import com.accor.kengine.Action;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.context.SimpleContext;
import org.springframework.stereotype.Component;

@Component
public class SimpleAction implements Action<FlowContextContainer> {

  @Override
  public void execute(FlowContextContainer flowContext) {
    SimpleContext context = flowContext.getContext(SimpleContext.class);

    context.setResult("Hello " + context.getInput() + " !");

  }
}
