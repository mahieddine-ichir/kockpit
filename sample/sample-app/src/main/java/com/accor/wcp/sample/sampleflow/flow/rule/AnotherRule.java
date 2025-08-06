package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.sample.sampleflow.flow.rule.action.SimpleAction;
import org.springframework.stereotype.Component;

import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.ACT_SIMPLE_ACTION;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.PRE_RANDOM_PREDICATE;

@Component
public class AnotherRule extends RuleNodesBuilderSupport<FlowContextContainer> {

  private final SimpleAction simpleAction;

  public AnotherRule(SimpleAction simpleAction) {
    super(BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2);
    this.simpleAction = simpleAction;
  }

  @Override
  public RuleNode<FlowContextContainer> configure() {
    RuleNodeBuilder<FlowContextContainer> builder = new RuleNodeBuilder<>(BR_WILL_SAY_HELLO_TO_USER_WITH_EXCLAMATION_MARK_ANOTHER_RULE2);
    // @formatter:off
    return builder
            .predicate(this::random, PRE_RANDOM_PREDICATE)
            .ok()
              .action(simpleAction, ACT_SIMPLE_ACTION)
              .predicate(this::random, PRE_RANDOM_PREDICATE)
              .ok()
                .action(simpleAction, ACT_SIMPLE_ACTION)
              .done()
            .done()
            .ko()
              .action(simpleAction, ACT_SIMPLE_ACTION)
            .done()
            .lastly()
              .action(simpleAction, ACT_SIMPLE_ACTION)
            .done()
            .createRuleNode();
    // @formatter:on
  }

  private boolean random(FlowContextContainer flowContextContainer) {
    return Math.random() * 10 < 5;
  }
}
