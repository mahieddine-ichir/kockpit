
package com.accor.wcp.sample.sampleflow.flow.rule;

import static com.accor.wcp.sample.sampleflow.flow.rule.BusinessRule.BR_CALLER_RULE;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.ACT_CALL_ACTION;
import static com.accor.wcp.sample.sampleflow.flow.rule.action.AppAction.ACT_CALL_NOAUDIT_ACTION;

import com.accor.kengine.Action;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import com.accor.wcp.flow.FlowRunner;
import com.accor.wcp.sample.sampleflow.AppFlowDocumentation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CallerRule extends RuleNodesBuilderSupport<FlowContextContainer> {

  private final ApplicationContext applicationContext;
  private FlowRunner flowRunner;

  public CallerRule(ApplicationContext applicationContext, @Lazy FlowRunner flowRunner) {
    super(BR_CALLER_RULE);
    this.applicationContext = applicationContext;
    this.flowRunner = flowRunner;
  }

  @Override
  public RuleNode<FlowContextContainer> configure() throws Exception {
    RuleNodeBuilder<FlowContextContainer> builder = new RuleNodeBuilder<>(getDetails());
    // @formatter:off
    return builder
            .action((Action<FlowContextContainer>) this::callAnotherFlow, ACT_CALL_ACTION)
            .action((Action<FlowContextContainer>) this::callAnotherFlowWithNoAudit, ACT_CALL_NOAUDIT_ACTION)
            .createRuleNode();
    // @formatter:on
  }

  private void callAnotherFlowWithNoAudit(FlowContextContainer flowContextContainer) {
    // This call is not audited
    flowRunner.execute(AppFlowDocumentation.FLOW_SUBFLOW_SLOW, flowContextContainer, "Not audited", false);
    // This call too
    flowRunner.execute(AppFlowDocumentation.FLOW_SUBFLOW_SLOW, flowContextContainer, false);
  }

  private void callAnotherFlow(FlowContextContainer flowContextContainer) {
    for (int i = 1; i < 3; i++) {
      flowRunner.execute(AppFlowDocumentation.FLOW_SUBFLOW_SLOW, flowContextContainer, "Call Number " + i);
    }
  }
}
