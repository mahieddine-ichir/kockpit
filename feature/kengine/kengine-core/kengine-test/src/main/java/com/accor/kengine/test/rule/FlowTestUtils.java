package com.accor.kengine.test.rule;

import static org.junit.jupiter.api.Assertions.fail;

import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.ErrorExecutionException;
import com.accor.kengine.RuleExecutionException;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.execution.RuleNodeExecution;
import com.accor.kengine.execution.StepExecution;
import com.accor.kengine.execution.StepType;
import com.accor.kengine.registry.RuleNodesBuilderSupport;
import com.accor.kengine.registry.model.Rule;
import com.accor.kengine.registry.seamless.RuleNodesBuilderSeamLessSupport;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;

public class FlowTestUtils {

    public static void assertThatHasNodeExecutionWithStepSize(RuleNodeExecution ruleNodeExecution, int stepSize) {
        Assertions.assertThat(ruleNodeExecution).isNotNull();
        Assertions.assertThat(ruleNodeExecution.getStepExecutions().size()).isEqualTo(stepSize);
    }

    public static void assertNominalSingleRuleNodeExecution(ExecutionResult executionResult) {
        Assertions.assertThat(executionResult).isNotNull();
        Assertions.assertThat(executionResult.isWarning()).isFalse();
        Assertions.assertThat(executionResult.isSuccessful()).isTrue();
        Assertions.assertThat(executionResult.getErrorStepExecution()).isNull();
        Assertions.assertThat(executionResult.getWarningStepExecution()).isNull();
        //assertThat(executionResult.getTimeInMs()).isLessThanOrEqualTo(60);
        Assertions.assertThat(executionResult.getRuleExecutions().size()).isEqualTo(1);
    }

    public static void assertErrorInSingleRuleNodeExecution(ExecutionResult executionResult, StepType stepTypeFailed, DocumentationDetails stepNameFailed) {
        Assertions.assertThat(executionResult).isNotNull();
        Assertions.assertThat(executionResult.isWarning()).isFalse();
        Assertions.assertThat(executionResult.isSuccessful()).isFalse();
        Assertions.assertThat(executionResult.getErrorStepExecution()).isNotNull();
        Assertions.assertThat(executionResult.getErrorStepExecution().getType()).isEqualTo(stepTypeFailed);
        Assertions.assertThat(executionResult.getErrorStepExecution().getDetails()).isEqualTo(stepNameFailed);
        Assertions.assertThat(executionResult.getWarningStepExecution()).isNull();
        Assertions.assertThat(executionResult.getRuleExecutions().size()).isEqualTo(1);
    }

    public static void assertWarningInSingleRuleNodeExecution(ExecutionResult executionResult, StepType stepTypeFailed, DocumentationDetails stepNameFailed) {
        Assertions.assertThat(executionResult).isNotNull();
        Assertions.assertThat(executionResult.isWarning()).isTrue();
        Assertions.assertThat(executionResult.isSuccessful()).isFalse();
        Assertions.assertThat(executionResult.getWarningStepExecution()).isNotNull();
        Assertions.assertThat(executionResult.getWarningStepExecution().getType()).isEqualTo(stepTypeFailed);
        Assertions.assertThat(executionResult.getWarningStepExecution().getDetails()).isEqualTo(stepNameFailed);
        Assertions.assertThat(executionResult.getErrorStepExecution()).isNull();
        Assertions.assertThat(executionResult.getRuleExecutions().size()).isEqualTo(1);
    }

    public static void assertThatStepExecutionSucceeded(StepExecution stepExecution, StepType stepTypeSuccess, DocumentationDetails stepNameSuccess) {
        Assertions.assertThat(stepExecution.isResult()).isTrue();
        Assertions.assertThat(stepExecution.isExecutionError()).isFalse();
        Assertions.assertThat(stepExecution.isExecutionWarning()).isFalse();
        Assertions.assertThat(stepExecution.getType()).isEqualTo(stepTypeSuccess);
        Assertions.assertThat(stepExecution.getDetails()).isEqualTo(stepNameSuccess);
        Assertions.assertThat(stepExecution.getThrowable()).isNull();
        Assertions.assertThat(stepExecution.getErrorMessage()).isNull();
    }

    public static void assertThatStepExecutionFailed(StepExecution stepExecution, StepType stepTypeFailed, DocumentationDetails stepNameFailed) {
        Assertions.assertThat(stepExecution.isResult()).isFalse();
        Assertions.assertThat(stepExecution.isExecutionError()).isFalse();
        Assertions.assertThat(stepExecution.isExecutionWarning()).isFalse();
        Assertions.assertThat(stepExecution.getType()).isEqualTo(stepTypeFailed);
        Assertions.assertThat(stepExecution.getDetails()).isEqualTo(stepNameFailed);
        Assertions.assertThat(stepExecution.getThrowable()).isNull();
        Assertions.assertThat(stepExecution.getErrorMessage()).isNull();
    }

    public static void assertThatStepExecutionFailedWithWarningAt(StepExecution stepExecution, String errorMessage, StepType stepType, DocumentationDetails stepName,
        Class<? extends Exception> warningRuleExceptionClass) {
        Assertions.assertThat(stepExecution.isExecutionError()).isFalse();
        Assertions.assertThat(stepExecution.isExecutionWarning()).isTrue();
        Assertions.assertThat(stepExecution.getType()).isEqualTo(stepType);
        Assertions.assertThat(stepExecution.getDetails()).isEqualTo(stepName);
        Assertions.assertThat(stepExecution.getThrowable()).isInstanceOf(warningRuleExceptionClass);
        Assertions.assertThat(stepExecution.getErrorMessage()).contains(errorMessage);
    }

    public static void assertThatStepExecutionFailedWithErrorAt(StepExecution stepExecution, String errorMessage, StepType stepType, DocumentationDetails stepName) {
        Assertions.assertThat(stepExecution.isExecutionError()).isTrue();
        Assertions.assertThat(stepExecution.isExecutionWarning()).isFalse();
        Assertions.assertThat(stepExecution.getType()).isEqualTo(stepType);
        Assertions.assertThat(stepExecution.getDetails()).isEqualTo(stepName);
        Assertions.assertThat(stepExecution.getThrowable()).isInstanceOf(ErrorExecutionException.class);
        Assertions.assertThat(stepExecution.getErrorMessage()).contains(errorMessage);
    }

    public static <T> ExecutionResult runFlow(T context, List<RuleNodesBuilderSupport<T>> flowRules) {
        // RuleNodes
        List<RuleNode> ruleNodes = flowRules.stream()
            .map(FlowTestUtils::apply)
            .toList();

        return runRuleNodes(context, ruleNodes);
    }

    public static ExecutionResult runNodes(Object context, List<RuleNodesBuilderSeamLessSupport> flowRules) {
        // RuleNodes
        List<RuleNode> ruleNodes = flowRules.stream()
            .map(FlowTestUtils::apply)
            .toList();

        return runRuleNodes(context, ruleNodes);
    }

    public static <T> ExecutionResult runRuleNode(T context, RuleNode ruleNode) {
        return runRuleNodes(context, Collections.singletonList(ruleNode));
    }

    public static <T> ExecutionResult runRuleNodes(T context, List<RuleNode> ruleNodes) {
        // Execute
        RuleNodeExecutor ruleNodeExecutor = new RuleNodeExecutor();
        return ruleNodeExecutor.execute(ruleNodes, context);
    }

    public static <T> void runRuleNodeWithWarningException(T context, RuleNodesBuilderSeamLessSupport support) throws Throwable {
        runRuleNodesWithWarningException(context, Collections.singletonList(support));
    }

    public static <T> void runRuleNodesWithWarningException(T context, List<RuleNodesBuilderSeamLessSupport> flowRules) throws Throwable {
        // Execute for a warning exception
        try {
            runNodes(context, flowRules);
            fail("Flow run must throw a warning exception");
        } catch (RuleExecutionException e) {
            throw e.getExecutionResult().getWarningStepExecution().getThrowable();
        }
    }

    public static <T> void runRuleNodeWithErrorException(T context, RuleNodesBuilderSeamLessSupport support) throws Throwable {
        runRuleNodesWithErrorException(context, Collections.singletonList(support));
    }

    public static <T> void runRuleNodesWithErrorException(T context, List<RuleNodesBuilderSeamLessSupport> flowRules) throws Throwable {
        // Execute for a warning exception
        try {
            runNodes(context, flowRules);
            fail("Flow run must throw an 'error' exception");
        } catch (RuleExecutionException e) {
            throw e.getExecutionResult().getErrorStepExecution().getThrowable();
        }
    }

    public static <T> void runFlowWithWarningException(T context, List<RuleNodesBuilderSupport<T>> flowRules) throws Throwable {
        // Execute for a warning exception
        try {
            runFlow(context, flowRules);
            fail("Flow run must throw a warning exception");
        } catch (RuleExecutionException e) {
            throw e.getExecutionResult().getWarningStepExecution().getThrowable();
        }
    }

    public static <T> void runFlowWithErrorException(T context, List<RuleNodesBuilderSupport<T>> flowRules) throws Throwable {
        // Execute for a warning exception
        try {
            runFlow(context, flowRules);
            fail("Flow run must throw an 'error' exception");
        } catch (RuleExecutionException e) {
            throw e.getExecutionResult().getErrorStepExecution().getThrowable();
        }
    }

    private static RuleNode apply(RuleNodesBuilderSupport contextRuleNodesBuilderSupport) {
        try {
            return contextRuleNodesBuilderSupport.configure();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static RuleNode apply(
        RuleNodesBuilderSeamLessSupport contextRuleNodesBuilderSupport) {
        try {
            return contextRuleNodesBuilderSupport.configure();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public static <T> ExecutionResult executeFlow(T context, List<Rule<T>> flowRules) {
        // RuleNodes
        List<RuleNode<T>> ruleNodes = new ArrayList<>();
        for (Rule<T> flowRule : flowRules) {
            RuleNode<T> ruleNode = flowRule.getRuleNode();
            ruleNodes.add(ruleNode);
        }

        // Execute
        RuleNodeExecutor<T> ruleNodeExecutor = new RuleNodeExecutor<>();
        return ruleNodeExecutor.execute(ruleNodes, context);
    }
}
