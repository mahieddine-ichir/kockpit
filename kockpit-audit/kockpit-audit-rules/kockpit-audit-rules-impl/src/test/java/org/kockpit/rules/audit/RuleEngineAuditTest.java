package org.kockpit.rules.audit;

import org.junit.Test;
import org.kockpit.rules.DefaultDocumentationDetails;
import org.kockpit.rules.RuleNode;
import org.kockpit.rules.RuleNodeBuilder;
import org.kockpit.rules.RulePredicate;
import org.kockpit.rules.execution.ExecutionResult;
import org.kockpit.rules.executor.RuleNodeExecutor;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class RuleEngineAuditTest {

  @Test
  public void compute() {
    ExecutionAudit audit = audit();
    assertNotNull(audit);
    // TODO add more asserts!
  }

  private ExecutionAudit audit() {
    // @formatter:off
    RulePredicate<Boolean> booleanRulePredicate = new RulePredicate<>(Boolean::booleanValue);

    RuleNodeBuilder<Boolean> builder =
            new RuleNodeBuilder<Boolean>()
                    .predicate(booleanRulePredicate)
                    .ok()
                    .predicate(booleanRulePredicate)
                    .actions(List.of(new StringAction("OK")))
                    .ok()
                    .predicate(booleanRulePredicate) //
                    .ok()
                    .predicate(booleanRulePredicate)
                    .actions(List.of(new StringAction("OK1")))
                    .done()
                    .ko()
                    .predicate(booleanRulePredicate)
                    .actions(List.of(new StringAction("KO")))
                    .done()
                    .done()
                    .ko()
                    .predicate(booleanRulePredicate)
                    .done() //
                    .done()
                    .ko()
                    .actions(List.of(new StringAction("KO")))
                    .done();
    // @formatter:on

    RuleNodeBuilder<Boolean> builderSkipped =
            new RuleNodeBuilder<Boolean>(b -> false, new DefaultDocumentationDetails("SKIPPED", "DOC"))
                    .predicate(booleanRulePredicate) //
                    .actions(List.of(new StringAction("OK1")));

    RuleNodeExecutor<Boolean> ruleNodeExecutor = new RuleNodeExecutor<>();
    ruleNodeExecutor.setActionExecutor((action, context) -> {}); // Do nothing
    RuleNode<Boolean> rootRuleNode = builder.createRuleNode();
    RuleNode<Boolean> rootRuleNodeSkipped = builderSkipped.createRuleNode();

    ExecutionResult executionResult =
            ruleNodeExecutor.execute(Arrays.asList(rootRuleNode, rootRuleNodeSkipped), Boolean.TRUE);

    RuleEngineAudit ruleEngineAudit = new RuleEngineAudit();
    return ruleEngineAudit.compute(executionResult);
  }
}
