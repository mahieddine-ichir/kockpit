package com.accor.kengine.testers.v32;

import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.perform;
import static com.accor.kengine.seamless.ConditionalRuleNodeSeamLessBuilder.StaticBuilder.when;

import com.accor.kengine.RuleNode;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import com.accor.kengine.seamless.Rule;

@Rule(value = "rule1", documentation = "")
class Rule1 extends RuleNodesBuilderSeamLessSupport {
  private final MySimplePredicate mySimplePredicate;

  private final MySimpleAction mySimpleAction;

  Rule1(MySimplePredicate mySimplePredicate, MySimpleAction mySimpleAction) {
    this.mySimplePredicate = mySimplePredicate;
    this.mySimpleAction = mySimpleAction;
  }

  @Override
  public RuleNode configure() {
    return when(mySimplePredicate).then(perform(mySimpleAction)).end();
  }
}
