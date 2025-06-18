package com.kockpit.rules.registry.registryappwithflow;

import com.kockpit.rules.*;
import com.kockpit.rules.registry.RuleNodesBuilderSupport;
import com.kockpit.rules.registry.registryapp.StringAction;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class RuleNodeBuilderFakeForTestFlow1 extends RuleNodesBuilderSupport<String> {

  public RuleNodeBuilderFakeForTestFlow1() {
    super(new DefaultDocumentationDetails("RuleNodeBuilderFakeForTestFlow1", "Doc"));
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
