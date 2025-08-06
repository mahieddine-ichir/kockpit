package com.accor.wcp.flow;

import com.accor.kengine.Action;
import com.accor.kengine.ActionExecutor;
import com.accor.kengine.DefaultActionExecutor;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.ErrorExecutionException;
import com.accor.kengine.ExecutionInterruptionException;
import com.accor.kengine.RuleExecutionException;
import com.accor.kengine.RuleInterruptionException;
import com.accor.kengine.RuleNode;
import com.accor.kengine.RuleNodeException;
import com.accor.kengine.RuleNodeExecutor;
import com.accor.kengine.RulePredicate;
import com.accor.kengine.SkipRuleException;
import com.accor.kengine.WarningExecutionException;
import com.accor.kengine.execution.ExecutionResult;
import com.accor.kengine.execution.RuleExecution;
import com.accor.kengine.execution.RuleNodeExecution;
import com.accor.kengine.execution.StepExecution;
import com.accor.kengine.execution.StepType;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

public class DefaultRuleNodeExecutorFactory implements RuleNodeExecutorFactory {

  @Slf4j
  protected static class DefaultRuleNodeExecutor<T> extends RuleNodeExecutor<T> {

    private static int incrementalPosition = 0;

    private final ActionExecutor<T> actionExecutor = new DefaultActionExecutor<>();

    private final String executionName;

    private final long creationTimestamp;

    private final int position;

    private final boolean audit;

    public DefaultRuleNodeExecutor() {
      this(null, true);
    }

    public DefaultRuleNodeExecutor(String executionName) {
      this(executionName, true);
    }

    public DefaultRuleNodeExecutor(String executionName, boolean audit) {
      this.position = DefaultRuleNodeExecutor.incrementalPosition++;
      this.creationTimestamp = System.currentTimeMillis();
      this.executionName = executionName;
      this.audit = audit;
    }

    public int getPosition() {
      return position;
    }

    public String getExecutionName() {
      return executionName;
    }

    public long getCreationTimestamp() {
      return creationTimestamp;
    }

    public boolean isAudit() {
      return audit;
    }

    @Override
    public ExecutionResult execute(
        List<RuleNode<T>> ruleNodes, T context, ExecutionResult executionResult) {
      List<RuleExecution> ruleExecutions = executionResult.getRuleExecutions();
      for (RuleNode<T> ruleNode : ruleNodes) {
        executeRuleNodeAndUpdateExecutionResult(context, executionResult, ruleExecutions, ruleNode);
      }
      terminateExecution(executionResult);
      return executionResult;
    }

    private void executeRuleNodeAndUpdateExecutionResult(
        T context,
        ExecutionResult executionResult,
        List<RuleExecution> ruleExecutions,
        RuleNode<T> ruleNode) {
      ExecutionResult nodeExecutionResult =
          logAndExecuteRuleNode(context, executionResult, ruleExecutions, ruleNode);
      executionResult.setWarning(executionResult.isWarning() || nodeExecutionResult.isWarning());
      executionResult.setThrowable(nodeExecutionResult.getThrowable());
    }

    private void terminateExecution(ExecutionResult executionResult) {
      executionResult.setSuccessful(true);
      executionResult.setEndTimestamp(System.currentTimeMillis());
    }

    private ExecutionResult logAndExecuteRuleNode(
        T context,
        ExecutionResult executionResult,
        List<RuleExecution> ruleExecutions,
        RuleNode<T> ruleNode) {
      log.debug("Starting {}", ruleNode.getDetails().toString());
      Optional<ExecutionResult> nodeExecutionResult =
          executeRuleNodeAndGetExecutionResult(context, executionResult, ruleExecutions, ruleNode);
      if (nodeExecutionResult.isPresent()) {
        return nodeExecutionResult.get();
      }
      log.debug("End of {}", ruleNode.getDetails().toString());
      return executionResult;
    }

    private Optional<ExecutionResult> executeRuleNodeAndGetExecutionResult(
        T context,
        ExecutionResult executionResult,
        List<RuleExecution> ruleExecutions,
        RuleNode<T> ruleNode) {
      try {
        long start = System.currentTimeMillis();
        RuleNodeExecution ruleNodeExecution = execute(ruleNode, context);
        RuleExecution ruleExecution = summarizeExecution(ruleNode, start, ruleNodeExecution);
        ruleExecutions.add(ruleExecution);
        updateExecutionResultWithWarnings(executionResult, ruleNodeExecution);

      } catch (SkipRuleException ignored) {
        log.debug("Skipping {}", ruleNode.getDetails().toString());
      } catch (RuleNodeException e) {
        log.debug("Error during {}", ruleNode.getDetails().toString());
        RuleExecution ruleExecution = new RuleExecution(ruleNode, e.getRuleNodeExecution(), e);
        ruleExecutions.add(ruleExecution);
        Optional<ExecutionResult> nodeExecutionErrorResult =
            buildExecutionResultWithError(executionResult, e);
        if (nodeExecutionErrorResult.isPresent()) {
          return nodeExecutionErrorResult;
        }
        throw new RuleExecutionException(ruleExecution, executionResult, e);
      }
      return Optional.empty();
    }

    private Optional<ExecutionResult> buildExecutionResultWithError(
        ExecutionResult executionResult, RuleNodeException e) {
      executionResult.setSuccessful(false);
      executionResult.setThrowable(e);
      executionResult.setEndTimestamp(System.currentTimeMillis());

      RuleNodeExecution ruleNodeExecutionError = e.getRuleNodeExecution();
      while (ruleNodeExecutionError.getNext() != null) {
        ruleNodeExecutionError = ruleNodeExecutionError.getNext();
      }
      executionResult.setWarning(ruleNodeExecutionError.isWarning());

      // Error step execution (keep tracking)
      if (ruleNodeExecutionError.getThrowable() instanceof ErrorExecutionException) {
        ErrorExecutionException cause =
            (ErrorExecutionException) ruleNodeExecutionError.getThrowable();
        executionResult.setErrorStepExecution(cause.getStepExecution());
      } else if (ruleNodeExecutionError.getThrowable() instanceof WarningExecutionException) {
        WarningExecutionException cause =
            (WarningExecutionException) ruleNodeExecutionError.getThrowable();
        executionResult.setWarningStepExecution(cause.getStepExecution());
        if (ruleNodeExecutionError.getThrowable() instanceof RuleInterruptionException) {
          executionResult.setThrowable(null);
          return Optional.of(executionResult);
        }
      } else if (ruleNodeExecutionError.getThrowable() instanceof ExecutionInterruptionException) {
        executionResult.setSuccessful(true);
        executionResult.setWarning(false);
        return Optional.empty();
      }
      return Optional.empty();
    }

    private void updateExecutionResultWithWarnings(
        ExecutionResult executionResult, RuleNodeExecution ruleNodeExecution) {
      while (ruleNodeExecution != null) {
        executionResult.setWarning(executionResult.isWarning() || ruleNodeExecution.isWarning());
        executionResult.setSuccessful(true);
        if (ruleNodeExecution.getThrowable() instanceof WarningExecutionException) {
          executionResult.addWarning((WarningExecutionException) ruleNodeExecution.getThrowable());
        }
        ruleNodeExecution = ruleNodeExecution.getNext();
      }
    }

    private RuleExecution summarizeExecution(
        RuleNode<T> ruleNode, long start, RuleNodeExecution ruleNodeExecution) {
      RuleExecution ruleExecution = new RuleExecution(ruleNode, ruleNodeExecution);
      ruleExecution.setStartTimestamp(start);
      ruleExecution.setEndTimestamp(System.currentTimeMillis());
      return ruleExecution;
    }

    @Override
    protected ExecutionResult executeRuleNode(
        T context,
        ExecutionResult executionResult,
        List<RuleExecution> ruleExecutions,
        RuleNode<T> ruleNode) {
      return logAndExecuteRuleNode(context, executionResult, ruleExecutions, ruleNode);
    }

    @Override
    protected void executeAction(T context, RuleNodeExecution ruleNodeExecution, Action<T> action) {
      executeActionAndTraceWarnings(context, ruleNodeExecution, action);
    }

    protected void executeActionAndTraceWarnings(
        T context, RuleNodeExecution ruleNodeExecution, Action<T> action) {
      DocumentationDetails details = action.getDetails();
      String name = "" + details;
      String classname = action.getClass().getName();
      long start = System.currentTimeMillis();
      try {
        actionExecutor.execute(action, context);
        StepExecution stepExecution =
            new StepExecution(StepType.ACTION, name, classname, true, details);

        stepExecution.setStartTimestamp(start);
        stepExecution.setEndTimestamp(System.currentTimeMillis());
        ruleNodeExecution.getStepExecutions().add(stepExecution);
      } catch (WarningExecutionException e) {
        // Warning
        log.debug("Action execution warning. Action: " + action, e);
        StepExecution stepExecution =
            new StepExecution(StepType.ACTION, name, classname, e, e.getMessage(), details);

        stepExecution.setStartTimestamp(start);
        stepExecution.setEndTimestamp(System.currentTimeMillis());
        stepExecution.setThrowable(e);
        ruleNodeExecution.getStepExecutions().add(stepExecution);
        ruleNodeExecution.setWarning(true);
        e.setStepExecution(stepExecution);
        ruleNodeExecution.setThrowable(e);
        if (e.isStopExecution()) {
          throw e;
        }
      } catch (ExecutionInterruptionException e) {
        log.debug("Interrupt execution silently. Action: " + action);
        StepExecution stepExecution =
            new StepExecution(StepType.ACTION, name, classname, true, details);

        stepExecution.setStartTimestamp(start);
        stepExecution.setEndTimestamp(System.currentTimeMillis());
        ruleNodeExecution.getStepExecutions().add(stepExecution);
        throw e;
      } catch (SkipRuleException e) {
        log.debug("Skipping action " + e.getMessage());
        throw e;
      } catch (Throwable t) {
        log.debug("Action execution error. Action: " + action, t);
        StepExecution stepExecution =
            new StepExecution(StepType.ACTION, name, classname, t, t.getMessage(), details);

        stepExecution.setStartTimestamp(start);
        stepExecution.setEndTimestamp(System.currentTimeMillis());
        ruleNodeExecution.getStepExecutions().add(stepExecution);

        // Special case to continue on exception
        if (t instanceof ContinueOnExceptionAble
            && ((ContinueOnExceptionAble) t).shouldContinueOnException()) {
          return;
        }

        // Keep track on step execution
        throw new ErrorExecutionException(t.getMessage(), t, stepExecution);
      }
    }

    @Override
    protected boolean executePredicate(
        T context,
        RuleNodeExecution ruleNodeExecution,
        boolean result,
        RulePredicate<T> predicate) {
      return super.executePredicate(context, ruleNodeExecution, result, predicate);
    }
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor() {
    return new DefaultRuleNodeExecutor<>();
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName) {
    return new DefaultRuleNodeExecutor<>(executionName);
  }

  @Override
  public <T> RuleNodeExecutor<T> createRuleNodeExecutor(String executionName, boolean audit) {
    return new DefaultRuleNodeExecutor<>(executionName, audit);
  }
}
