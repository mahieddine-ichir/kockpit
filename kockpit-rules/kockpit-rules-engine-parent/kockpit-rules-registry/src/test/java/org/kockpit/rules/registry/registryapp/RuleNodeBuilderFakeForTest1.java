package org.kockpit.rules.registry.registryapp;

import org.kockpit.rules.DefaultDocumentationDetails;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.RuleNodeBuilder;
import org.kockpit.rules.RulePredicate;
import org.kockpit.rules.registry.RuleNodesBuilderSupport;
import org.springframework.stereotype.Component;

import java.util.Arrays;

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
