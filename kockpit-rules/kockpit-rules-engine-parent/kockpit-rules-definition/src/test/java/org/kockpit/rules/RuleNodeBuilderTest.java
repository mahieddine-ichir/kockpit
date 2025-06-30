package org.kockpit.rules;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Slf4j
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
            .actions(List.of(new StringAction("OK")))
            .done()
            .ko()
            .actions(List.of(new StringAction("KO")))
            .done()
            //                    .ko().predicate(booleanRulePredicate) //
            .done() //
            //                .ko().predicate(booleanRulePredicate) //
            .createRuleNode(); //
    // @formatter:on

    assertThat(ruleNode).isNotNull();
    assertThat(ruleNode.getOk()).isNotNull();
    assertThat(ruleNode.getOk().getKo()).isNotNull();

    log.info("registry nodes: {}", ruleNode);
  }
}
