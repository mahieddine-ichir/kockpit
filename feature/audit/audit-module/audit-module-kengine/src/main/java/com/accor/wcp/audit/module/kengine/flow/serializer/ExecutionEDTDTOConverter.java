package com.accor.wcp.audit.module.kengine.flow.serializer;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toMap;

import com.accor.kengine.audit.model.ActionPredicateExecution;
import com.accor.kengine.audit.model.Execution;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.RuleExecution;
import com.accor.wcp.sdk.application.SdkApplicationProperties;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;

public class ExecutionEDTDTOConverter {

  private static final String RULE_NAME_BRROOT = "BRROOT";
  private final SdkApplicationProperties sdkApplicationProperties;

  public ExecutionEDTDTOConverter(SdkApplicationProperties sdkApplicationProperties) {
    this.sdkApplicationProperties = sdkApplicationProperties;
  }

  public ExecutionEDTDTO convert(Execution execution) {
    ExecutionEDTDTO executionEDTDTO = new ExecutionEDTDTO();
    executionEDTDTO.setDate(execution.getDate());
    executionEDTDTO.setTime(execution.getTime());
    executionEDTDTO.setError(execution.getResultStatus().name());
    executionEDTDTO.setErrorDetails(execution.getDetail());
    executionEDTDTO.setErrorMessage(execution.getMessage());
    executionEDTDTO.setExecutionUUID(execution.getExecutionUuid());

    List<? extends RuleExecution> ruleExecutions = execution.getRules();
    List<RuleEDTDTO> executionRules =
        ruleExecutions.stream()
            .sorted(comparingInt(RuleExecution::getPosition))
            .filter(rule -> !RULE_NAME_BRROOT.equals(rule.getName()))
            .map(this::convertToRuleEDTDTO)
            .toList();
    executionEDTDTO.setExecutionRules(executionRules);

    Map<String, List<RuleDetailEDTDTO>> ruleDetails =
        ruleExecutions.stream()
            .sorted(comparingInt(RuleExecution::getPosition))
            .filter(rule -> !RULE_NAME_BRROOT.equals(rule.getName()))
            .map(this::convertToKeyListRuleDetails)
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    executionEDTDTO.setRules(ruleDetails);
    executionEDTDTO.setRegistryId(execution.getRegistryId());

    // New manage full registry ID in application
    String fullRegistryReferentialId = getFullRegistryReferentialId(execution);
    executionEDTDTO.setFullRegistryReferentialId(fullRegistryReferentialId);

    return executionEDTDTO;
  }

  AbstractMap.SimpleEntry<String, List<RuleDetailEDTDTO>> convertToKeyListRuleDetails(
      RuleExecution ruleExecution) {
    List<RuleDetailEDTDTO> ruleDetailEDTDTOS =
        ruleExecution.getActionPredicates().stream().map(this::convertToRuleDetailEDTDTO).toList();
    String name = ruleExecution.getName();
    if (name == null) {
      name = "NONAME-" + ruleExecution.getId();
    }
    return new AbstractMap.SimpleEntry<>(name, ruleDetailEDTDTOS);
  }

  RuleDetailEDTDTO convertToRuleDetailEDTDTO(ActionPredicateExecution actionPredicate) {
    RuleDetailEDTDTO ruleDetailEDTDTO = new RuleDetailEDTDTO();
    ruleDetailEDTDTO.setDate(actionPredicate.getDate());
    ruleDetailEDTDTO.setTime(actionPredicate.getTime());
    if (actionPredicate.getResultStatus() == ResultStatus.ERROR
        || actionPredicate.getResultStatus() == ResultStatus.WARNING) {
      ruleDetailEDTDTO.setError(actionPredicate.getResultStatus().name());
    }
    if (actionPredicate.getResultStatus() == ResultStatus.CONDITION_FALSE) {
      ruleDetailEDTDTO.setCondition(false);
    }
    if (actionPredicate.getResultStatus() == ResultStatus.CONDITION_TRUE) {
      ruleDetailEDTDTO.setCondition(true);
    }
    ruleDetailEDTDTO.setErrorDetails(actionPredicate.getDetail());
    ruleDetailEDTDTO.setErrorMessage(actionPredicate.getMessage());

    ruleDetailEDTDTO.setName(actionPredicate.getCode());
    ruleDetailEDTDTO.setDetail(actionPredicate.getName());
    ruleDetailEDTDTO.setActionPredicate(actionPredicate.getTypeAP().name());
    return ruleDetailEDTDTO;
  }

  RuleEDTDTO convertToRuleEDTDTO(RuleExecution rule) {
    RuleEDTDTO ruleEDTDTO = new RuleEDTDTO();
    ruleEDTDTO.setDate(rule.getDate());
    ruleEDTDTO.setTime(rule.getTime());
    ruleEDTDTO.setError(rule.getResultStatus().name());
    ruleEDTDTO.setErrorDetails(rule.getDetail());
    ruleEDTDTO.setErrorMessage(rule.getMessage());

    ruleEDTDTO.setName(rule.getCode());
    ruleEDTDTO.setDetail(rule.getName());

    return ruleEDTDTO;
  }

  private String getFullRegistryReferentialId(Execution execution) {
    return sdkApplicationProperties.getDomain()
        + "-"
        + sdkApplicationProperties.getApplicationEnv()
        + "-"
        + sdkApplicationProperties.getApplicationId()
        + "-"
        + execution.getRegistryId();
  }
}
