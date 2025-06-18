package com.kockpit.rules;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class RuleNodeBuilderTest {

  @Test
  void should_create_rulenode_tree() {
    // @formatter:off
    RulePredicate<Boolean> booleanRulePredicate = new RulePredicate<>(Boolean::booleanValue);
    RuleNode<Boolean> ruleNode =
        new RuleNodeBuilder<Boolean>()
            .predicate(booleanRulePredicate) //
            .ok()
            .predicate(booleanRulePredicate) //
            .ok()
            .predicate(booleanRulePredicate) //
            .ok()
            .actions(Arrays.asList(new StringAction("OK")))
            .done()
            .ko()
            .actions(Arrays.asList(new StringAction("KO")))
            .done()
            //                    .ko().predicate(booleanRulePredicate) //
            .done() //
            //                .ko().predicate(booleanRulePredicate) //
            .createRuleNode(); //
    // @formatter:on

    assertThat(ruleNode).isNotNull();
    assertThat(ruleNode.getOk()).isNotNull();
    assertThat(ruleNode.getOk().getKo()).isNotNull();
    System.out.println("registry nodes: " + ruleNode);
  }
}
