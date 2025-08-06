package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.rule.action.SimpleAction;
import org.springframework.stereotype.Component;

import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.ACT_SIMPLE_ACTION;

@Component
public class SimpleRule extends RuleNodesBuilderSupport<FlowContextContainer> {

  private final SimpleAction simpleAction;

  public SimpleRule(SimpleAction simpleAction) {
    super(BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_SIMPLE_RULE);
    this.simpleAction = simpleAction;
  }

  @Override
  public RuleNode<FlowContextContainer> configure() throws Exception {
    RuleNodeBuilder<FlowContextContainer> builder = new RuleNodeBuilder<>(getDetails());
    // @formatter:off
    return builder.action(simpleAction, ACT_SIMPLE_ACTION).createRuleNode();
    // @formatter:on
  }
}
