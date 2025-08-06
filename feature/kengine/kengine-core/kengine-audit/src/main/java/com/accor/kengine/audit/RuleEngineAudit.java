package com.accor.kengine.audit;

import static com.accor.kengine.audit.model.ResultStatus.ERROR;
import static com.accor.kengine.audit.model.ResultStatus.WARNING;

import com.accor.kengine.DetailHandler;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.SimpleDetail;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.execution.*;
import java.util.ArrayList;
import java.util.List;

public class RuleEngineAudit {

  private DetailHandler detailHandler = new DefaultDetailHandler();

  public RuleEngineAudit() {}

  public RuleEngineAudit(DetailHandler detailHandler) {
    this.detailHandler = detailHandler;
  }

  public void setDetailHandler(DetailHandler detailHandler) {
    this.detailHandler = detailHandler;
  }

  public ExecutionAudit compute(ExecutionAudit executionAudit, ExecutionResult executionResult) {
    List<RuleExecutionAudit> rules = computeRules(executionAudit, executionResult);
    executionAudit.setRules(rules);
    return executionAudit;
  }

  public ExecutionAudit compute(ExecutionResult executionResult) {
    ExecutionAudit executionEDT = compute(new ExecutionAudit(executionResult), executionResult);
    ResultStatus resultStatus;
    if (!executionResult.isSuccessful()) {
      if (executionResult.isWarning()) {
        resultStatus = WARNING;
      } else {
        resultStatus = ERROR;
      }
    } else {
      if (executionResult.isWarning()) {
        resultStatus = ResultStatus.WARNING;
      } else {
        resultStatus = ResultStatus.VALID;
      }
    }
    executionEDT.setResultStatus(resultStatus);

    // Warning step?
    StepExecution warningStepExecution = executionResult.getWarningStepExecution();
    if (warningStepExecution != null) {
      SimpleDetail detail = detailHandler.handle(warningStepExecution.getDetails());
      if (StepType.ACTION.equals(warningStepExecution.getType())) {
        executionEDT.setErrorActionCode(detail.getCode());
      } else {
        executionEDT.setErrorPredicateCode(detail.getCode());
      }
    }

    // Error step?
    StepExecution errorStepExecution = executionResult.getErrorStepExecution();
    if (errorStepExecution != null) {
      SimpleDetail detail = detailHandler.handle(errorStepExecution.getDetails());
      if (StepType.ACTION.equals(errorStepExecution.getType())) {
        executionEDT.setErrorActionCode(detail.getCode());
      } else {
        executionEDT.setErrorPredicateCode(detail.getCode());
      }
    }

    return executionEDT;
  }

  private List<RuleExecutionAudit> computeRules(
      ExecutionAudit executionAudit, ExecutionResult executionResult) {
    List<RuleExecution> ruleExecutions = executionResult.getRuleExecutions();

    List<RuleExecutionAudit> ruleEDTList = new ArrayList<>(ruleExecutions.size());
    for (RuleExecution ruleExecution : ruleExecutions) {
      RuleExecutionAudit ruleEDT = this.computeRuleExecution(executionAudit, ruleExecution);
      if (ruleEDT.getResultStatus() == ResultStatus.WARNING) {
        executionResult.setWarning(true);
        executionAudit.getWarningRuleCodes().add(ruleEDT.getCode());
        executionAudit.getWarningRuleCodeMessages().add(ruleEDT.getMessage());
      } else if (ruleEDT.getResultStatus() == ResultStatus.ERROR) {
        executionAudit.setErrorRuleCode(ruleEDT.getCode());
      }
      ruleEDT.setPosition(executionAudit.getRules().size() + ruleEDTList.size());
      ruleEDTList.add(ruleEDT);
    }

    return ruleEDTList;
  }

  private RuleExecutionAudit computeRuleExecution(
      ExecutionAudit executionAudit, RuleExecution ruleExecution) {
    RuleExecutionAudit ruleEDT = new RuleExecutionAudit(executionAudit, ruleExecution);
    setDetails(ruleExecution, ruleEDT);

    // Compute steps
    computeRuleNodeExecution(ruleEDT, ruleExecution.getRuleNodeExecution());

    return ruleEDT;
  }

  /**
   * Recursive computing.
   *
   * @param ruleEDT
   * @param ruleNodeExecution
   */
  private void computeRuleNodeExecution(
      RuleExecutionAudit ruleEDT, RuleNodeExecution ruleNodeExecution) {
    for (StepExecution stepExecution : ruleNodeExecution.getStepExecutions()) {
      ActionPredicateAudit actionPredicateEDT = new ActionPredicateAudit(ruleEDT, stepExecution);
      setDetails(stepExecution, actionPredicateEDT);
      actionPredicateEDT.setPosition(ruleEDT.getActionPredicates().size());
      ruleEDT.getActionPredicates().add(actionPredicateEDT);
    }

    // Recursive path
    if (ruleNodeExecution.getNext() != null) {
      computeRuleNodeExecution(ruleEDT, ruleNodeExecution.getNext());
    }
  }

  private void setDetails(RuleExecution ruleExecution, RuleExecutionAudit ruleEDT) {
    DocumentationDetails details = ruleExecution.getRuleNode().getDetails();
    SimpleDetail simpleDetail = detailHandler.handle(details);
    ruleEDT.setCode(simpleDetail.getCode());
    ruleEDT.setName(simpleDetail.getName());
  }

  private void setDetails(StepExecution stepExecution, ActionPredicateAudit actionPredicateEDT) {
    SimpleDetail simpleDetail = detailHandler.handle(stepExecution.getDetails());
    actionPredicateEDT.setCode(simpleDetail.getCode());
    actionPredicateEDT.setName(simpleDetail.getName());
  }
}
