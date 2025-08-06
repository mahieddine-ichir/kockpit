package com.accor.kengine.registry.seamless.v32;

import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.RulePredicate;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.kengine.registry.seamless.action.StringAction;
import com.accor.kengine.seamless.Rule;
import java.util.Arrays;

@Rule(documentation = "Rule 2 Sample example")
public class Rule2 extends RuleNodesBuilderSupport {

  @Override
  public RuleNode configure() {
    // @formatter:off
    RulePredicate<Boolean> booleanRulePredicate = new RulePredicate<>(Boolean::booleanValue);
    RuleNodeBuilder<Boolean> builder =
        new RuleNodeBuilder<Boolean>()
            .predicate(booleanRulePredicate) //
            .ok()
            .actions(Arrays.asList(new StringAction("OK"))) //
            .done();
    // @formatter:on
    return builder.createRuleNode();
  }
}
