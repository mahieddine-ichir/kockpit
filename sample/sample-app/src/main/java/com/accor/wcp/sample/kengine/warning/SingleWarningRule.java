package com.accor.wcp.sample.kengine.warning;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;
import static com.accor.wcp.flow.errors.WcpError.TECHNICAL_ERROR;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Action;
import com.accor.kengine.seamless.Rule;
import com.accor.wcp.flow.errors.FlowExecutionWarning;
import lombok.AllArgsConstructor;

@Rule
@AllArgsConstructor
class SingleWarningRule extends RuleNodesBuilderSeamLessSupport {

  @Override
  public RuleNode configure() {
    WarningAction warningAction = new WarningAction();

    return perform(warningAction).end();
  }

  static class WarningAction {
    @Action
    String throwWarning() {
      throw new FlowExecutionWarning(TECHNICAL_ERROR);
    }
  }
}
