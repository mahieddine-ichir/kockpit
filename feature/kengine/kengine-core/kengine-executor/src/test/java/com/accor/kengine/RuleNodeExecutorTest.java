package com.accor.kengine;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.junit.jupiter.api.Assertions.*;

import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.execution.RuleExecution;
import com.accor.kengine.execution.RuleNodeExecution;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

public class RuleNodeExecutorTest {

  @Test
  public void execute() throws RuleNodeException {
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

    RuleNodeExecutor<Boolean> ruleNodeExecutor = new RuleNodeExecutor<>();
    ruleNodeExecutor.setActionExecutor((action, context) -> {}); // Do nothing
    RuleNode<Boolean> rootRuleNode = builder.createRuleNode();

    RuleNodeExecution ruleNodeExecutionTrue = ruleNodeExecutor.execute(rootRuleNode, TRUE);
    RuleNodeExecution ruleNodeExecutionFalse = ruleNodeExecutor.execute(rootRuleNode, FALSE);

    assertTrue(ruleNodeExecutionTrue.isOk());
    assertTrue(ruleNodeExecutionTrue.getNext().isOk());
    assertTrue(ruleNodeExecutionTrue.getNext().next().isOk());

    assertFalse(ruleNodeExecutionFalse.isOk());
    assertTrue(ruleNodeExecutionFalse.getNext().isOk()); // No Predicate
    assertNull(ruleNodeExecutionFalse.getNext().getNext());
    assertEquals(1, ruleNodeExecutionFalse.getNext().getStepExecutions().size());
    assertTrue(ruleNodeExecutionFalse.getNext().getStepExecutions().get(0).isResult());
    assertFalse(ruleNodeExecutionFalse.getNext().getStepExecutions().get(0).isExecutionError());

    // TODO remove sout and use a test unit for execution data asserts :)
    System.out.println(ruleNodeExecutionTrue);
    System.out.println(ruleNodeExecutionFalse);
  }

  @Test
  public void should_skip_rule() {
    // @formatter:off
    RuleNodeBuilder<Boolean> skipped_builder =
        new RuleNodeBuilder<Boolean>()
            .ok()
            .action(
                new SkipAction(),
                new DefaultDocumentationDetails(SkipAction.class.getSimpleName(), null))
            .done();
    RuleNodeBuilder<Boolean> executed_builder =
        new RuleNodeBuilder<Boolean>()
            .ok()
            .action(
                new StringAction("OK"),
                new DefaultDocumentationDetails(StringAction.class.getSimpleName(), null))
            .done();
    // @formatter:on

    RuleNodeExecutor<Boolean> ruleNodeExecutor = new RuleNodeExecutor<>();
    ruleNodeExecutor.setActionExecutor(Action::execute);
    RuleNode<Boolean> skippedRuleNode = skipped_builder.createRuleNode();
    RuleNode<Boolean> executedRuleNode = executed_builder.createRuleNode();

    // Wrap skipped rulenode with executed rule nodes so we're sure it doesn't interrupt execution
    // of a list of rules
    ExecutionResult executionResult =
        ruleNodeExecutor.execute(
            Arrays.asList(executedRuleNode, skippedRuleNode, executedRuleNode), true);
    assertEquals(2, executionResult.getRuleExecutions().size());
    RuleExecution firstRuleExecution = executionResult.getRuleExecutions().get(0);
    RuleExecution secondRuleExecution = executionResult.getRuleExecutions().get(1);
    assertTrue(firstRuleExecution.getRuleNodeExecution().isOk());
    assertTrue(firstRuleExecution.getRuleNodeExecution().getNext().isOk());
    assertEquals(firstRuleExecution.getRuleNodeExecution().getNext().getStepExecutions().size(), 1);
    assertEquals(
        firstRuleExecution.getRuleNodeExecution().getNext().getStepExecutions().get(0).getName(),
        "StringAction");
    assertTrue(secondRuleExecution.getRuleNodeExecution().isOk());
    assertTrue(secondRuleExecution.getRuleNodeExecution().getNext().isOk());
    assertEquals(
        secondRuleExecution.getRuleNodeExecution().getNext().getStepExecutions().get(0).getName(),
        "StringAction");
  }

  @Test
  public void should_skip_rule_v2() {
    // @formatter:off
    RuleNodeBuilder<Boolean> skipped_builder =
        new RuleNodeBuilder<Boolean>(b -> false, new DefaultDocumentationDetails("SKIP", "Doc"))
            .ok()
            .action(
                new StringAction("OK"),
                new DefaultDocumentationDetails(StringAction.class.getSimpleName(), null))
            .done();
    RuleNodeBuilder<Boolean> executed_builder =
        new RuleNodeBuilder<Boolean>()
            .ok()
            .action(
                new StringAction("OK"),
                new DefaultDocumentationDetails(StringAction.class.getSimpleName(), null))
            .done();
    // @formatter:on

    RuleNodeExecutor<Boolean> ruleNodeExecutor = new RuleNodeExecutor<>();
    ruleNodeExecutor.setActionExecutor(Action::execute);
    RuleNode<Boolean> skippedRuleNode = skipped_builder.createRuleNode();
    RuleNode<Boolean> executedRuleNode = executed_builder.createRuleNode();

    // Wrap skipped rulenode with executed rule nodes so we're sure it doesn't interrupt execution
    // of a list of rules
    ExecutionResult executionResult =
        ruleNodeExecutor.execute(
            Arrays.asList(executedRuleNode, skippedRuleNode, executedRuleNode), true);
    assertEquals(3, executionResult.getRuleExecutions().size());
    RuleExecution firstRuleExecution = executionResult.getRuleExecutions().get(0);
    RuleExecution secondRuleExecution = executionResult.getRuleExecutions().get(1);
    RuleExecution thirdRuleExecution = executionResult.getRuleExecutions().get(2);
    assertTrue(secondRuleExecution.getRuleNodeExecution().isSkipped());
    assertTrue(firstRuleExecution.getRuleNodeExecution().isOk());
    assertTrue(firstRuleExecution.getRuleNodeExecution().getNext().isOk());
    assertEquals(firstRuleExecution.getRuleNodeExecution().getNext().getStepExecutions().size(), 1);
    assertEquals(
        firstRuleExecution.getRuleNodeExecution().getNext().getStepExecutions().get(0).getName(),
        "StringAction");
    assertTrue(thirdRuleExecution.getRuleNodeExecution().isOk());
    assertTrue(thirdRuleExecution.getRuleNodeExecution().getNext().isOk());
    assertEquals(
        thirdRuleExecution.getRuleNodeExecution().getNext().getStepExecutions().get(0).getName(),
        "StringAction");
  }
}
