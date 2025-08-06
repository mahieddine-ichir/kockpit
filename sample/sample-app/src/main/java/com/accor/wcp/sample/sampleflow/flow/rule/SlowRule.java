
package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.Action;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import org.springframework.stereotype.Component;

import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_SLOW_RULE;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.ACT_SLOW_ACTION;

@Component
public class SlowRule extends RuleNodesBuilderSupport<FlowContextContainer> {

  public SlowRule() {
    super(BR_SLOW_RULE);
  }

  @Override
  public RuleNode<FlowContextContainer> configure() {
    RuleNodeBuilder<FlowContextContainer> builder = new RuleNodeBuilder<>(getDetails());
    // @formatter:off
    return builder.action((Action<FlowContextContainer>) this::slow, ACT_SLOW_ACTION).createRuleNode();
    // @formatter:on
  }

  private void slow(FlowContextContainer flowContextContainer) {
    try {
      Thread.sleep(3000);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
}
