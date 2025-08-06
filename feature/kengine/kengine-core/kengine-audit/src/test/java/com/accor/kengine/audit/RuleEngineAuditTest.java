package com.accor.kengine.audit;

import static org.junit.Assert.assertNotNull;

import com.accor.kengine.*;
import com.accor.kengine.execution.ExecutionResult;
import java.util.Arrays;
import org.junit.Test;

public class RuleEngineAuditTest {

  public ExecutionAudit audit() {
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

    RuleNodeBuilder<Boolean> builderSkipped =
        new RuleNodeBuilder<Boolean>(b -> false, new DefaultDocumentationDetails("SKIPPED", "DOC"))
            .predicate(booleanRulePredicate) //
            .actions(Arrays.asList(new StringAction("OK1")));

    RuleNodeExecutor<Boolean> ruleNodeExecutor = new RuleNodeExecutor<>();
    ruleNodeExecutor.setActionExecutor((action, context) -> {}); // Do nothing
    RuleNode<Boolean> rootRuleNode = builder.createRuleNode();
    RuleNode<Boolean> rootRuleNodeSkipped = builderSkipped.createRuleNode();

    ExecutionResult executionResult =
        ruleNodeExecutor.execute(Arrays.asList(rootRuleNode, rootRuleNodeSkipped), Boolean.TRUE);

    RuleEngineAudit ruleEngineAudit = new RuleEngineAudit();
    ExecutionAudit executionEDT = ruleEngineAudit.compute(executionResult);
    return executionEDT;
  }

  @Test
  public void compute() {
    ExecutionAudit audit = audit();
    assertNotNull(audit);
    // TODO add more asserts!
  }
}
