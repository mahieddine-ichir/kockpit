package com.accor.kengine.registry.seamless.v32;

import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeBuilder;
import com.accor.kengine.RulePredicate;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.kengine.registry.seamless.action.StringAction;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
@Deprecated
public class OldCompatibilityRule extends RuleNodesBuilderSupport {

  static final String BR_OLD_EXAMPLE = "BR_OLD_EXAMPLE";

  public OldCompatibilityRule() {
    super(new DefaultDocumentationDetails(BR_OLD_EXAMPLE, "My old compatibility rule example"));
  }

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
