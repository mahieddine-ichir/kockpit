package com.accor.kengine.testers.v32;

import com.accor.kengine.*;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class OldCompatibilityRule2 extends RuleNodesBuilderSupport {

  public OldCompatibilityRule2() {
    super(new DefaultDocumentationDetails("rule2", "Doc for rule2"));
  }

  @Override
  public RuleNode configure() {
    // @formatter:off
    RulePredicate<Boolean> booleanRulePredicate =
        new RulePredicate<>(Boolean::booleanValue, getDetails());
    RuleNodeBuilder<Boolean> builder =
        new RuleNodeBuilder<Boolean>(getDetails())
            .predicate(booleanRulePredicate) //
            .ok()
            .actions(Arrays.asList(new StringAction("OK"))) //
            .done();
    // @formatter:on
    return builder.createRuleNode();
  }
}
