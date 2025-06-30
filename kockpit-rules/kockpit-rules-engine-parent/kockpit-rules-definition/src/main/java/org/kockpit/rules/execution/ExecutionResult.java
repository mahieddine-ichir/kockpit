package org.kockpit.rules.execution;

import org.kockpit.rules.RuleNodeException;
import org.kockpit.rules.WarningExecutionException;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
public class ExecutionResult {

  @Setter
  private String executionId;

  private List<RuleExecution> ruleExecutions;

  @Setter
  private boolean successful;

  @Setter
  private boolean warning;

  @Setter
  private RuleNodeException throwable;

  private final List<WarningExecutionException> warnings = new ArrayList<>();

  @Setter
  private long startTimestamp;

  @Setter
  private long endTimestamp;

  @Setter
  private StepExecution errorStepExecution;

  @Setter
  private StepExecution warningStepExecution;

  public ExecutionResult(List<RuleExecution> ruleExecutions) {
    this.executionId = UUID.randomUUID().toString();
    this.ruleExecutions = ruleExecutions;
    startTimestamp = System.currentTimeMillis();
  }

  public void addWarning(WarningExecutionException e) {
    warnings.add(e);
  }

  public long getTimeInMs() {
    return endTimestamp - startTimestamp;
  }

}
