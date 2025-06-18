package com.kockpit.rules.executor;

import com.kockpit.rules.*;
import com.kockpit.rules.execution.*;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * @deprecated While be removed / hidden in KEngine 4.0
 * @param <T>
 */
@Deprecated(since = "3.2.0", forRemoval = true)
@NoArgsConstructor
public class RuleNodeExecutor<T> {

  private KengineLogStore kengineLogStore;

  public RuleNodeExecutor(KengineLogStore kengineLogStore) {
    this.kengineLogStore = kengineLogStore;
  }

  private final Logger logger = LoggerFactory.getLogger(getClass());

  @Setter
  private ActionExecutor<T> actionExecutor = new DefaultActionExecutor<>();

    public ExecutionResult execute(
          List<RuleNode<T>> ruleNodes, T context, ExecutionResult executionResult) {
    List<RuleExecution> ruleExecutions = executionResult.getRuleExecutions();
    for (RuleNode<T> ruleNode : ruleNodes) {
      ExecutionResult nodeExecutionResult =
          executeRuleNode(context, executionResult, ruleExecutions, ruleNode);
      if (nodeExecutionResult != null) {
        return nodeExecutionResult;
      }
    }
    // Success on it
    executionResult.setSuccessful(true);
    executionResult.setEndTimestamp(System.currentTimeMillis());

    return executionResult;
  }

  protected ExecutionResult executeRuleNode(
      T context,
      ExecutionResult executionResult,
      List<RuleExecution> ruleExecutions,
      RuleNode<T> ruleNode) {
    logToKengine("Starting ", ruleNode.getDetails().toString());
    long start = System.currentTimeMillis();
    try {
      RuleNodeExecution ruleNodeExecution = execute(ruleNode, context);
      RuleExecution ruleExecution = new RuleExecution(ruleNode, ruleNodeExecution);
      ruleExecution.setStartTimestamp(start);
      ruleExecution.setEndTimestamp(System.currentTimeMillis());
      ruleExecutions.add(ruleExecution);
    } catch (SkipRuleException ignored) {
      logToKengine("Skipping ", ruleNode.getDetails().toString());
    } catch (RuleNodeException e) {
      logToKengine("Error during ", ruleNode.getDetails().toString());
      RuleExecution ruleExecution = new RuleExecution(ruleNode, e.getRuleNodeExecution(), e);
      ruleExecutions.add(ruleExecution);
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
          return null;
        }
      } else if (ruleNodeExecutionError.getThrowable() instanceof ExecutionInterruptionException) {
        executionResult.setSuccessful(true);
        executionResult.setWarning(false);
        return executionResult;
      }

      throw new RuleExecutionException(ruleExecution, executionResult, e);
    }
    logToKengine("End of ", ruleNode.getDetails().toString());
    return null;
  }

  public ExecutionResult execute(List<RuleNode<T>> ruleNodes, T context) {
    List<RuleExecution> ruleExecutions = new LinkedList<>();
    ExecutionResult executionResult = new ExecutionResult(ruleExecutions);
    return execute(ruleNodes, context, executionResult);
  }

  public RuleNodeExecution execute(RuleNode<T> ruleNode, T context) throws RuleNodeException {
    // Audit
    RuleNodeExecution ruleNodeExecution = new RuleNodeExecution(ruleNode);
    long start = System.currentTimeMillis();
    ruleNodeExecution.setStartTimestamp(start);

    // Skip rule execution?
    if (!ruleNode.getEligibilityPredicate().test(context)) {
      ruleNodeExecution.setSkipped(true);
      return ruleNodeExecution;
    }

    // Current rule node
    List<Action<T>> actions = new ArrayList<>();
    List<Action<T>> ruleActions = ruleNode.getActions();
    if (ruleActions != null) {
      actions.addAll(ruleActions);
      try {
        handleActions(ruleActions, context, ruleNodeExecution);
      } catch (WarningExecutionException e) {
        ruleNodeExecution.setThrowable(e);
        ruleNodeExecution.setEndTimestamp(System.currentTimeMillis());
        ruleNodeExecution.setWarning(true);
        throw new RuleNodeException(ruleNodeExecution, e);
      } catch (ErrorExecutionException | ExecutionInterruptionException e) {
        ruleNodeExecution.setThrowable(e);
        ruleNodeExecution.setEndTimestamp(System.currentTimeMillis());
        throw new RuleNodeException(ruleNodeExecution, e);
      }
    }

    // Next one
    List<RulePredicate<T>> predicates = ruleNode.getPredicates();
    boolean ok;
    try {
      ok = predicates.isEmpty() || this.test(predicates, context, ruleNodeExecution);
    } catch (ErrorExecutionException e) {
      // Error
      ruleNodeExecution.setThrowable(e);
      ruleNodeExecution.setEndTimestamp(System.currentTimeMillis());
      throw new RuleNodeException(ruleNodeExecution, e);
    }
    RuleNode<T> next;
    if (ok) {
      next = ruleNode.getOk();
    } else {
      next = ruleNode.getKo();
    }

    // Audit
    ruleNodeExecution.setOk(ok);
    ruleNodeExecution.setEndTimestamp(System.currentTimeMillis());

    // Browse next (deeper)
    if (next != null) {
      try {
        RuleNodeExecution nextRuleNodeExecution = execute(next, context);
        ruleNodeExecution.setNext(nextRuleNodeExecution);
      } catch (RuleNodeException e) {
        ruleNodeExecution.setNext(e.getRuleNodeExecution());
        throw new RuleNodeException(ruleNodeExecution, e);
      }
    }
    if (ruleNode.getLastly() != null) {
      try {
        ruleNodeExecution.getTerminalNode().setNext(execute(ruleNode.getLastly(), context));
      } catch (RuleNodeException e) {
        ruleNodeExecution.getTerminalNode().setNext(e.getRuleNodeExecution());
        throw new RuleNodeException(ruleNodeExecution, e);
      }
    }

    return ruleNodeExecution;
  }

  private boolean test(
      List<RulePredicate<T>> predicates, T context, RuleNodeExecution ruleNodeExecution) {

    boolean result = true;
    for (RulePredicate<T> predicate : predicates) {
      result = executePredicate(context, ruleNodeExecution, result, predicate);
    }
    // All must be true to validate test
    return result;
  }

  protected boolean executePredicate(
      T context, RuleNodeExecution ruleNodeExecution, boolean result, RulePredicate<T> predicate) {
    long start = System.currentTimeMillis();
    try {
      boolean test = predicate.getPredicate().test(context);
      result = result && test;
      StepExecution stepExecution = getStepExecution(predicate, test);
      stepExecution.setStartTimestamp(start);
      stepExecution.setEndTimestamp(System.currentTimeMillis());
      ruleNodeExecution.getStepExecutions().add(stepExecution);
    } catch (WarningExecutionException e) {
      // Warning
      logger.warn("Predicate execution warning. Predicate: " + predicate, e);
      StepExecution stepExecution = getStepExecution(predicate, false);
      stepExecution.setExecutionWarning(true);
      stepExecution.setStartTimestamp(start);
      stepExecution.setEndTimestamp(System.currentTimeMillis());
      ruleNodeExecution.getStepExecutions().add(stepExecution);
      if (e.isStopExecution()) {
        throw e;
      }
      result = false;
    } catch (Throwable t) {
      logger.error("Can't execute predicate: " + predicate, t);
      StepExecution stepExecution = getStepExecution(predicate, false);
      stepExecution.setThrowable(t);
      stepExecution.setStartTimestamp(start);
      stepExecution.setEndTimestamp(System.currentTimeMillis());
      ruleNodeExecution.getStepExecutions().add(stepExecution);

      // Keep track on step execution
      throw new ErrorExecutionException(t.getMessage(), t, stepExecution);
    }
    return result;
  }

  private StepExecution getStepExecution(RulePredicate<T> predicate, Boolean result) {
    Object details = predicate.getDetails();
    String name = "" + details;
    String predicateClassname = predicate.getPredicate().getClass().getName();
    return new StepExecution(
        StepType.PREDICATE, name, predicateClassname, result, predicate.getDetails());
  }

  protected void handleActions(
      List<Action<T>> ruleActions, T context, RuleNodeExecution ruleNodeExecution) {
    if (actionExecutor == null) {
      logger.warn("No action configured executor, skipping actions execution");
      return;
    }
    for (Action<T> action : ruleActions) {
      executeAction(context, ruleNodeExecution, action);
    }
  }

  protected void executeAction(T context, RuleNodeExecution ruleNodeExecution, Action<T> action) {
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
      logger.debug("Action execution warning. Action: " + action, e);
      StepExecution stepExecution =
          new StepExecution(StepType.ACTION, name, classname, e, e.getMessage(), details);

      stepExecution.setStartTimestamp(start);
      stepExecution.setEndTimestamp(System.currentTimeMillis());
      ruleNodeExecution.getStepExecutions().add(stepExecution);

      e.setStepExecution(stepExecution);

      if (e.isStopExecution()) {
        throw e;
      }
    } catch (ExecutionInterruptionException e) {
      logger.debug("Interrupt execution silently. Action: {}", action);
      StepExecution stepExecution =
          new StepExecution(StepType.ACTION, name, classname, true, details);

      stepExecution.setStartTimestamp(start);
      stepExecution.setEndTimestamp(System.currentTimeMillis());
      ruleNodeExecution.getStepExecutions().add(stepExecution);
      throw e;
    } catch (SkipRuleException e) {
      logger.debug("Skipping action {}", e.getMessage());
      throw e;
    } catch (Throwable t) {
      logger.debug("Action execution error. Action: " + action, t);
      StepExecution stepExecution =
          new StepExecution(StepType.ACTION, name, classname, t, t.getMessage(), details);

      stepExecution.setStartTimestamp(start);
      stepExecution.setEndTimestamp(System.currentTimeMillis());
      ruleNodeExecution.getStepExecutions().add(stepExecution);

      // Keep track on step execution
      throw new ErrorExecutionException(t.getMessage(), t, stepExecution);
    }
  }

  private void logToKengine(String ruleNodeStatus, String ruleNodeName) {
    if (kengineLogStore != null) {
      kengineLogStore
          .getLogs()
          .add(
              KengineLog.builder()
                  .date(new Date())
                  .action(ruleNodeName)
                  .log(ruleNodeStatus)
                  .build());
    }
  }
}
