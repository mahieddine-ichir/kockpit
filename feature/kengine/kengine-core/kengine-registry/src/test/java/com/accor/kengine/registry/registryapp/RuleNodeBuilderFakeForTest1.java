package com.accor.kengine.registry.registryapp;

import com.accor.kengine.*;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import java.util.Arrays;
import org.springframework.stereotype.Component;

@Component
public class RuleNodeBuilderFakeForTest1 extends RuleNodesBuilderSupport<String> {

  public RuleNodeBuilderFakeForTest1() {
    super(new DefaultDocumentationDetails("RuleNodeBuilderFakeForTest1", ""));
  }

  @Override
  public RuleNode configure() {
    // @formatter:off
    RulePredicate<Boolean> booleanRulePredicate = new RulePredicate<>(Boolean::booleanValue);
    RuleNodeBuilder<Boolean> builder =
        new RuleNodeBuilder<Boolean>()
            .predicate(booleanRulePredicate) //
            .ok()
            .predicate(booleanRulePredicate)
            .actions(Arrays.asList(new StringAction("OK"))) //
            .ok()
            .predicate(booleanRulePredicate) //
            .ok()
            .predicate(booleanRulePredicate)
            .actions(Arrays.asList(new StringAction("OK1")))
            .done()
            .ko()
            .predicate(booleanRulePredicate)
            .actions(Arrays.asList(new StringAction("KO")))
            .done()
            .done()
            .ko()
            .predicate(booleanRulePredicate)
            .done() //
            .done()
            .ko()
            .actions(Arrays.asList(new StringAction("KO")))
            .done();
    // @formatter:on
    return builder.createRuleNode();
  }
}
