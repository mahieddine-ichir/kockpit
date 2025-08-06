package com.accor.wcp.sample.sampleflow.flow.rule;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.wcp.flow.FlowContextContainer;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractRuleTest<A extends RuleNodesBuilderSupport<FlowContextContainer>> {

  A underTest;

  @SneakyThrows
  List<RuleNode<FlowContextContainer>> getRuleNodeList(A rule) {
    RuleNode<FlowContextContainer> ruleNode = rule.configure();
    List<RuleNode<FlowContextContainer>> ruleNodeList = new ArrayList<>();
    ruleNodeList.add(ruleNode);
    return ruleNodeList;
  }

  void executeFlow(FlowContextContainer context) {
    RuleNodeExecutor<FlowContextContainer> ruleNodeExecutor = new RuleNodeExecutor<>();
    ruleNodeExecutor.execute(getRuleNodeList(underTest), context);
  }
}
